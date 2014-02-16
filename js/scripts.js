LeapManager.init({
    enableMetaGestures: false,
    enableDefaultMetaGestureActions: false,
    maxCursors:1,
    enableHoverTap: true,
    enablePressDown: true,
    enableScrollbarScrolling: true
});

$(document).ready(function() {

    // $(this).on(gesture,'#demo', function(e) {
    //     $('.lcd').addClass('success');
    //     $('.display').addClass('success');
    // });

});

// var gesture2 = document.querySelector("div.outputbs3");

document.body.addEventListener("leap-swipe", function(event){
    var gesture = event.gesture;
    console.log(event.gesture + gesture.type + " : " + gesture.direction + "\n");
    // outputbs3.innerHTML += gesture.type + " : " + gesture.direction + "<br/>";
})


$(document).ready(function() {
    //set your functions up here
    swiped = false;
    grabPebble = function() {
        $(".pebble").addClass("grabbed");
        $(".android").addClass("grabbed");
        console.log("Pebble grabbed");
        console.log("Phone grabbed");
        swiped = true;
    }; 
    swipePhone = function() {
        if(swiped == true){
            $(".pebble").addClass("activated");
            $(".android").addClass("activated");
            console.log("Phone activated");
        }
    }; 

    //put the functions into an array
    var events = {
        "onGrab" : grabPebble, 
        "onSwipe" : swipePhone
    }
    
    //update any number of them at once
    $().leap("setEvents",events);  
});

document.body.addEventListener("grabbed", function(event){
    console.log("Phone activated");
})


