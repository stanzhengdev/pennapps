#include <pebble.h>

#define SAMPLE_HISTORY_SIZE 30
#define GESTURE_TIMER 3000

static void set_timer();
static void timer_handler();
static void check_for_gesture();
static void gesture_handler();
static void gesture_reset();
static void toggle_accelerometer();
static void handle_accel(AccelData *accel_data, uint32_t num_samples);

#define NUM_MENU_SECTIONS 1
#define NUM_FIRST_MENU_ITEMS 3

static Window *window;
static Window *appWindow;
static TextLayer *first_name_layer;
static TextLayer *last_name_layer;

static SimpleMenuLayer *simple_menu_layer;
static SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];
static SimpleMenuItem first_menu_items[NUM_FIRST_MENU_ITEMS];

static AppTimer *timer;
static int frequency = 100;
bool running = false;
bool gesture = false;
int axis = 0;
int16_t gesture_countdown = GESTURE_TIMER;
AccelData sample_history[SAMPLE_HISTORY_SIZE];
static int sample_history_index = 0;

enum { GESTURE_KEY, APP_KEY };

/* accelerometer stuff */

static void set_timer(){
  if(running){
    timer = app_timer_register(frequency, timer_handler, NULL);
  }
}

static void toggle_accelerometer(){
  running = !running;
  if (running){
    accel_service_set_sampling_rate(ACCEL_SAMPLING_10HZ);
    accel_data_service_subscribe(0, handle_accel);
  }
  else{
    accel_data_service_unsubscribe();
  }
  
  set_timer();
}


static void appWindow_load(Window *window) {
  int num_a_items = 0;
  first_menu_items[num_a_items++] = (SimpleMenuItem){
    // You should give each menu item a title and callback
    .title = "Pump X = Camera",
  };
  // The menu items appear in the order saved in the menu items array
  first_menu_items[num_a_items++] = (SimpleMenuItem){
    .title = "Shake Y = Justice",
  };
  first_menu_items[num_a_items++] = (SimpleMenuItem){
    .title = "Knock Z = Video",
  };

  // Bind the menu items to the corresponding menu sections
  menu_sections[0] = (SimpleMenuSection){
    .num_items = NUM_FIRST_MENU_ITEMS,
    .items = first_menu_items,
  };

  Layer *appWindow_layer = window_get_root_layer(window);
  GRect bounds = layer_get_frame(appWindow_layer);

  // Initialize the simple menu layer
  simple_menu_layer = simple_menu_layer_create(bounds, window, menu_sections, NUM_MENU_SECTIONS, NULL);

  // Add it to the window for display
  layer_add_child(appWindow_layer, simple_menu_layer_get_layer(simple_menu_layer));

}

static void appWindow_unload(Window *window) {
  simple_menu_layer_destroy(simple_menu_layer);
}

//Select to view gesture commands
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  appWindow = window_create();
  //window_set_click_config_provider(appWindow, click_config_provider);
  window_set_window_handlers(appWindow, (WindowHandlers) {
    .load = appWindow_load,
    .unload = appWindow_unload,
  });
  const bool animated = true;
  window_stack_push(appWindow, animated);
}

//Dummy Handlers
static void up_click_handler(ClickRecognizerRef recognizer, void *context) { }
static void down_click_handler(ClickRecognizerRef recognizer, void *context) { }
static void handle_accel(AccelData *accel_data, uint32_t num_samples) { }


//Appmessage handlers
static void in_received_handler(DictionaryIterator* iter, void* context){
  // Tuple *gesture_tuple = dict_find(iter, GESTURE_KEY);
  // Tuple *app_tuple = dict_find(iter, APP_KEY);
  
  // char* gesture_string = gesture_tuple->value->cstring;
  // char* app_string = app_tuple->value->cstring;
  
  // text_layer_set_text(first_name_layer, first_name_string);
  // text_layer_set_text(last_name_layer, last_name_string); 
}

static void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context)
{
//  if (reason == APP_MSG_NOT_CONNECTED)
//  {
//    text_layer_set_text(first_name_layer, "Not");
//    text_layer_set_text(last_name_layer, "connected!");
//  }
  
  text_layer_set_text(first_name_layer, "Message");
  text_layer_set_text(last_name_layer, "failure");
}

//timer handler
static void timer_handler(){
  if (!running) return;
  
  AccelData current_sample = { .x = 0, .y = 0, .z = 0 };
  accel_service_peek(&current_sample);
  
  sample_history[sample_history_index].x = current_sample.x;
  sample_history[sample_history_index].y = current_sample.y;
  sample_history[sample_history_index].z = current_sample.z;
  
  sample_history_index++;
  
  if (sample_history_index >= SAMPLE_HISTORY_SIZE){
    sample_history_index = 0;
    check_for_gesture();
  }
  
  if (gesture){
    gesture_countdown -= frequency;
    if (gesture_countdown <= 0){
      gesture_reset();      
    }
  }
  
  set_timer();
}

static void gesture_handler(){
  gesture = true;
  
  //write and send a dictionary to the phone
  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);
  if(axis == 1){
    dict_write_cstring(iter, 0, "x");  
  }
  else if(axis == 2){
    dict_write_cstring(iter, 0, "y");  
  }
  else if(axis == 3){
    dict_write_cstring(iter, 0, "z");  
  }
  app_message_outbox_send();
}

static void gesture_reset(){
  gesture = false;
  gesture_countdown = GESTURE_TIMER;
  text_layer_set_text(first_name_layer, "Pebble");
  text_layer_set_text(last_name_layer, "Justice");
}

/* gesture detection */

static void check_for_gesture(){
  int16_t gesture_variance = 2500;
  
  //Initiating Shake X Gesture
  int16_t recent_max_x = 0;
  int16_t recent_min_x = 0;

  //Initiating Shake Y Gesture
  int16_t recent_max_y = 0;
  int16_t recent_min_y = 0;

  //Initiating Shake Z Gesture
  int16_t recent_max_z = 0;
  int16_t recent_min_z = 0;

  //checking for shake X
  for (int i = 0; i < SAMPLE_HISTORY_SIZE; i++){
    
    if (sample_history[i].x > recent_max_x){
      recent_max_x = sample_history[i].x;
    } 
    
    if (sample_history[i].x < recent_min_x){
      recent_min_x = sample_history[i].x;
    }

    if (sample_history[i].y > recent_max_y){
      recent_max_y = sample_history[i].y;
    } 
    
    if (sample_history[i].y < recent_min_y){
      recent_min_y = sample_history[i].y;
    }

    if (sample_history[i].z > recent_max_z){
      recent_max_z = sample_history[i].z;
    } 
    
    if (sample_history[i].z < recent_min_z){
      recent_min_z = sample_history[i].z;
    }

    if (recent_max_x - recent_min_x > gesture_variance){
      text_layer_set_text(first_name_layer, "Open X");
      text_layer_set_text(last_name_layer, "Camera App");
      vibes_short_pulse();
      axis = 1;
      gesture_handler("x");
    }

    else if (recent_max_y - recent_min_y > gesture_variance){
      text_layer_set_text(first_name_layer, "Open Y");
      text_layer_set_text(last_name_layer, "Justice App");
      vibes_short_pulse();
      axis = 2;
      gesture_handler("y");
    }
    else if (recent_max_z - recent_min_z > gesture_variance){
      text_layer_set_text(first_name_layer, "Open Z");
      text_layer_set_text(last_name_layer, "Video App");
      vibes_short_pulse();
      axis = 3;
      gesture_handler("z");
    }
  }

}



static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

    /* set up name text layers */
  int first_origin_x = 10;
  int first_width = bounds.size.w - first_origin_x;
  int first_height = 36;
  int first_origin_y = bounds.size.h / 2 - first_height;

  int last_width = bounds.size.w - 10;
  int last_origin_x = 0;
  int last_height = 32;
  int last_origin_y = bounds.size.h / 2 - 10;
  

    //init first name layer; set: background color, text color, text, alignment, font; add to window
  first_name_layer = text_layer_create((GRect){ .origin = { first_origin_x, first_origin_y }, .size = { first_width, first_height } });
  text_layer_set_background_color(first_name_layer, GColorClear);
  text_layer_set_text_color(first_name_layer, GColorWhite);
  text_layer_set_text(first_name_layer, "Pebble");
  text_layer_set_text_alignment(first_name_layer, GTextAlignmentLeft);
  text_layer_set_font(first_name_layer, fonts_get_system_font(FONT_KEY_BITHAM_30_BLACK));
  layer_add_child(window_layer, text_layer_get_layer(first_name_layer));
  
    //init last name layer; set: background color, text color, text, alignment, font; add to window
  last_name_layer = text_layer_create((GRect){ .origin = { last_origin_x, last_origin_y}, .size = { last_width, last_height } } );
  text_layer_set_background_color(last_name_layer, GColorClear);
  text_layer_set_text_color(last_name_layer, GColorWhite);
  text_layer_set_text(last_name_layer, "Justice");
  text_layer_set_text_alignment(last_name_layer, GTextAlignmentRight);
  text_layer_set_font(last_name_layer, fonts_get_system_font(FONT_KEY_GOTHIC_28));
  layer_add_child(window_layer, text_layer_get_layer(last_name_layer));
}

static void window_unload(Window *window) {
  text_layer_destroy(first_name_layer);
  text_layer_destroy(last_name_layer);
}

static void init(void) {
  window = window_create();
  window_set_background_color(window, GColorBlack);
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);

  for (int i = 0; i < SAMPLE_HISTORY_SIZE; i++){
    AccelData dummy_accel_values = { .x = 0, .y = 0, .z = 0 };
    sample_history[i] = dummy_accel_values;
  }
  set_timer();
  toggle_accelerometer();
}

static void app_message_init(){
  app_message_register_inbox_received(in_received_handler);
  app_message_register_outbox_failed(out_failed_handler);
  app_message_open(124, 124);
}

static void deinit(void) {
  if (running) toggle_accelerometer();
  accel_data_service_unsubscribe();
  window_destroy(window);
}

int main(void) {
  init();
  app_message_init();
  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

  app_event_loop();
  deinit();
}
