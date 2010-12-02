
var lifemap = {
  markers: []
}


/**
 * start here!
 * the document ready function
 */
$(function() {

  $.get("/form.html", function(data) {
    lifemap.html = data
  })

  $.get("/display.html", function(data) {
    lifemap.display = data
  })

  ajaxErrors()

  $("#profile").hide()
  $("#login").hide()


  // press esc to close an infowindow
  $(document.documentElement).keyup(function (event) {
    if (event.keyCode == 27) {
      closeInfoWindow()
    }
  })

  initHeavies()


})


function initHeavies() {

  lifemap.map = new google.maps.Map(
    document.getElementById("map_canvas"),
    {
      zoom: 5,
      mapTypeId: google.maps.MapTypeId.ROADMAP,
      mapTypeControl: false,
      navigationControl: false,
      streetViewControl: false
    })

  //mapStyle()

  setPosition(function(lat, lon) {
    lifemap.map.setCenter(new google.maps.LatLng(lat, lon))
  })

  facebookLoginLogout()
}

function addMapListener(f) {

  if(lifemap.mapListener) {
    google.maps.event.removeListener(lifemap.mapListener)
  }
  
  lifemap.mapListener = google.maps.event.addListener(lifemap.map, 'click', function(event) {
    f(event)
  })

}

function facebookLoginLogout() {

  FB.init({
    appId: '1e9fd626ddc803cfbc24ded85c2fb1b8',
    status: true,
    cookie: true,
    xfbml: true
  })

  FB.getLoginStatus(handleSessionResponse)

  FB.Event.subscribe('auth.sessionChange', handleSessionResponse)

  $("#logout").click(function(event) {
    event.preventDefault()
    FB.logout(function(response) {
      removeMarkers()
      forAllMarkers()
    })
  })

}

function ajaxErrors() {
  $("#msg").ajaxError(function(event, req, ajaxOptions, thrownError) {
    $("#add").hide()
    $(this).empty()
      .append("<li>Error requesting page " + ajaxOptions.url + "</li>")
      .append("<li>Error requesting page " + req + "</li>")
      .append("<li>Error requesting page " + event + "</li>")
      .append("<li>Error requesting page " + thrownError + "</li>")
  })
}

function setPosition(posfn) {

  if (navigator.geolocation) {

    navigator.geolocation.getCurrentPosition(

      function(position) {
        posfn(position.coords.latitude, position.coords.longitude)
      },

      function() {
        posfn(0, 0)
      }
    )
  } else {
    posfn(0, 0)
  }
}

function handleSessionResponse(response) {

  if (!response || !response.session) {
    $("#login").show().css("top","20px")
    $("#profile").hide()
    showBox( $("#intro"), $("#login") )
    addMapListener(mapClickedNotLoggedIn)
  } else {
    lifemap.id = FB.getSession().uid
    $("#profile").show().css("top","20px")
    $("#login").hide()
    $("#intro").hide()
    getList()
    addMapListener(mapClicked)
  }
}

function showBox(box, above) {
  var height = above.outerHeight()
  var offset = above.offset()
  box.show().css("top", (height + offset.top) + "px").css("width", "25%")
}

function getList() {
  $.post("/list", {
    fbid: lifemap.id
  }, getListCallback)
}

function getListCallback(response) {
  if (! response || response.length == 0) {
    showBox( $("#noevents"), $("#profile") )
  } else {
    // $("#slider-around").toggle()
    show(response)
  }
}


function forAllMarkers() {

  if(lifemap.markers.length > 1) {
    $("#slider-around").show()
    $("#noevents").hide()
  } else {
    $("#slider-around").hide()
    $("#noevents").show()
  }
  
  var markerBounds = new google.maps.LatLngBounds()

  var maxdate = Number.MIN_VALUE
  var mindate = Number.MAX_VALUE

  for(var i = 0; i < lifemap.markers.length; i ++) {

    lifemap.markers[i].setVisible(true)

    if(lifemap.markers[i].data.when > maxdate) maxdate = lifemap.markers[i].data.when
    if(lifemap.markers[i].data.when < mindate) mindate = lifemap.markers[i].data.when

    markerBounds.extend(lifemap.markers[i].getPosition())
  }

  lifemap.map.fitBounds(markerBounds)

  if(lifemap.markers.length < 2) {
    lifemap.map.setZoom(5)
  }


  $("#slider").slider( "destroy")

  $("#slider").slider({
    range: true,
    min: mindate,
    max: maxdate,
    values: [ mindate, maxdate ],
    slide: function(event, ui) {
      slideShowHide(ui)
    }
  })

  showSliderDate(mindate, maxdate)
  placeDots(mindate, maxdate)
}

function placeDots(mindate, maxdate) {

  $('img[src="img/green.png"]').detach()
  var dotbox = $("#slider-dots")
  var slider = $("#slider")
  var width = slider.outerWidth(true)
  var offset = slider.offset()
  var range = maxdate - mindate
  for(var i = 0; i < lifemap.markers.length; i ++) {
    var proportion = (lifemap.markers[i].data.when - mindate) / range
    var x = (proportion * width) - 16 - (i * 32)
    var img = $("<img src='img/green.png' height='32' width='32'/>")
    img.css("position","relative").css("left", x).css("z-index", 1)
    dotbox.append(img)


    var over = function(i) {
      return function() {
        var style = lifemap.markers[i].styleIcon
        style.set("color","#00ff00")
        lifemap.markers[i].setZIndex(1)
        this.src="img/green-glow.png"
      }
      
    }

    var out = function(i) {
      return function() {
        var style = lifemap.markers[i].styleIcon
        style.set("color","#c2f5a8")
        style.set("z-index","320")
        this.src="img/green.png"
      }
    }


    img.hover(over(i), out(i))

  }
}


function slideShowHide(ui) {
  
  for(var i = 0; i < lifemap.markers.length; i++) {
    if(lifemap.markers[i].data.when  >= ui.values[0] && lifemap.markers[i].data.when  <= ui.values[1]) {
      lifemap.markers[i].setVisible(true)
    } else {
      lifemap.markers[i].setVisible(false)
    }
  }

  showSliderDate(ui.values[0], ui.values[1])
  
}


function show(response) {

  for (var i = 0; i < response.length; i++) {

    var d1 = new Date(response[i].when)
    var pos = new google.maps.LatLng(response[i].lat, response[i].lng)


    var styledIcon = new StyledIcon(
      StyledIconTypes.BUBBLE,
      {
        color: "c2f5a8",
        text: response[i].tag + ", " + d1.getFullYear()
      })

    var marker = new StyledMarker(
      {
        styleIcon: styledIcon,
        position:  pos,
        map: lifemap.map
      })

    marker.listener = google.maps.event.addListener(marker, 'click', markerClicked(marker, pos))
    marker.data = response[i]
    lifemap.markers.push(marker)

  }

  forAllMarkers()

}

function markerClicked (marker, pos) {

  return function() {

    closeInfoWindow()

    var html = $(lifemap.display)
    html.find("#tag").append(marker.data.tag)
    if(blank(marker.data.desc)) {
      html.find("#description-label").detach()
    } else {
      html.find("#description").append(marker.data.desc)
    }
    html.find("#date").append(getDate(marker.data.when))

    lifemap.infowindow = new google.maps.InfoWindow({
      position: pos,
      content: html.wrap("<div id='display'/>").parent().html()
    })

    lifemap.infowindow.open(lifemap.map)


    google.maps.event.addListener(lifemap.infowindow, 'domready', function () {


      $("#edit").click(function(event) {
        event.preventDefault()
        closeInfoWindow()
        lifemap.infowindow = new google.maps.InfoWindow({
          position: pos,
          content: lifemap.html
        })
        

        google.maps.event.addListener(lifemap.infowindow, 'domready', function () {
          var date = new Date(marker.data.when)
          $("#tag").val(marker.data.tag)
          $("#id").val(marker.data.id)
          $("#description").val(marker.data.desc)
          $("#year").val(date.getFullYear())
          $("#month").val(date.getMonth() + 1)
          $("#day").val(date.getDate())
          $("#submit").val("Update")


          setTimeout(function() {
            $("#tag").focus()
          }, 500)

        })
        lifemap.infowindow.open(lifemap.map)

        $("#submit").click(function(ev) {
          ev.preventDefault()
          add_update("/update", pos, function(response) {
            marker.setMap(null)
            closeInfoWindow()
            removeMarker(marker)
            show(response)
          })
        })

      })

      $("#delete").click(function(event) {
        event.preventDefault()
        $("#display").after("<input id='cancelDelete' type='submit' value='Cancel'>")
        $("#display").after("<input id='confirmDelete' type='submit' value='Yes, Delete'>")
        $("#display").after("<div>Are you sure you wish to delete this?</div>")
        $("#display").toggle()
        setTimeout(function() {
          $("#confirmDelete").focus()
        }, 500)

        $("#confirmDelete").click(function(event) {
          event.preventDefault()
          $.post("/delete",{
            id: marker.data.id
          }, function(response) {
            marker.setMap(null)
            closeInfoWindow()
            removeMarker(marker)
            forAllMarkers()
          })

        })

        $("#cancelDelete").click(function() {
          closeInfoWindow()
        })
      })
    })
  }
}


function getDate(timestamp) {
  return new Date(timestamp).toDateString()
}

function showSliderDate(t1, t2) {
  $("#slider-date").empty().append(getDate(t1)).append(" to ").append(getDate(t2))
}


function resetMarker(event) {

  if (lifemap.marker && ! lifemap.marker.peristent) {
    lifemap.marker.setMap(null)
  }


  lifemap.marker = new google.maps.Marker({
    position: event.latLng,
    icon: "img/green-dot.png",
    shadow: "img/shadow.png",
    map: lifemap.map
  })

}

function closeInfoWindow() {
  if (lifemap.infowindow) {
    lifemap.infowindow.close()
  }
}

function mapClickedNotLoggedIn(event) {

  closeInfoWindow()


  lifemap.infowindow = new google.maps.InfoWindow({
    position: event.latLng,
    content: "Please login to start placing events on the map"
  })

  lifemap.infowindow.open(lifemap.map)

}

function mapClicked(event) {

  closeInfoWindow()

  lifemap.infowindow = new google.maps.InfoWindow({
    position: event.latLng,
    content: lifemap.html
  })


  lifemap.infowindow.open(lifemap.map)

  google.maps.event.addListener(lifemap.infowindow, 'domready', function () {

    setTimeout(function() {
      $("#tag").focus()
    }, 500)

    $("input").click(function() {
      $(this).css("background-color","transparent")
    })

    $("#description").keypress(function() {
      $("#count").remove()
      $(this).after( $("<span id='count'></span>").append(200 - $(this).val().length))
    })

    $("#month").css("color","gray").click(function() {
      this.empty()
    }).val("mm")
    $("#day").css("color","gray").click(function() {
      this.empty()
    }).val("dd")
    $("#year").click(function() {
      if("yyyy" == $(this).val()) {
        $(this).val("")
      }
    }).val("yyyy")


    $("#submit").click(function(ev) {
      ev.preventDefault()
      add_update("/add", event.latLng, added)
    })

    
  })


}


function blank(s) {
  return s == null || (/^\s*$/).test(s)
}

function makeDate(y, m, d) {
  if(blank(y)) {
    return null
  }
  else if(blank(m)) {
    return new Date(y,0,1)
  } else if(blank(d)) {
    return new Date(y, m-1,1)
  } else {
    return new Date(y, m-1, d)
  }
}


function add_update(url, latLng, callback) {
  var tag = $("#tag").val()
  var y = $("#year").val()
  var m = $("#month").val()
  var d = $("#day").val()
  var desc = $("#description").val()
  var id = $("#id").val()

  if("yyyy" == y) y = null
  if("dd" == d) d = null
  if("mm" == m) m = null
  var when = null;
  try {
    when = makeDate(y,m,d)
  } catch (err) {}

  if(errs(tag, y, when, desc)) {
    return
  }

  $.post(
    url,
    {
      id: id,
      tag: tag,
      desc: desc,
      when: when.getTime(),
      fbid: lifemap.id,
      lat: latLng.lat(),
      lng: latLng.lng()
    },
    callback)

}

function errs(tag, y, date, desc) {

  if(blank(tag)) {
    markErr("tag")
    return true
  } else if(blank(y)) {
    markErr("year")
    return true
  } else if ( ! date || ! isValidDate(date) ) {
    markErr("year","month","day")
    return true
  } else if( desc && desc.length > 200) {
    return true
  }

  return false
}

function isValidDate(d) {
  if ( Object.prototype.toString.call(d) !== "[object Date]" )
    return false;
  return !isNaN(d.getTime());
}

function markErr() {
  for(var i = 0; i< arguments.length; i++) {
    $("#" + arguments[i]).css("background-color","#FFC2CE")
  }


}

function added(data) {
  lifemap.infowindow.close()
  show(data)
}


function removeMarkers () {
  lifemap.markers = []
  for (var i = 0; i < lifemap.markers.length; i++) {
    google.maps.event.removeListener(lifemap.markers[i].listener)
  }
}


function removeMarker (marker) {
  for (var i = 0; i < lifemap.markers.length; i++) {
    if(marker.data.id === lifemap.markers[i].data.id) {
      lifemap.markers.splice(i,1)
      break
    }
  }
  google.maps.event.removeListener(marker.listener)
}

function mapStyle() {
  var stylez = [
    {
      featureType: "landscape",
      elementType: "all",
      stylers: [
        {
          hue: "#ffcc33"
        },

        {
          lightness: -20
        }
      ]
    },{
      featureType: "water",
      elementType: "all",
      stylers: [
        {
          hue: "#33ff99"
        },

        {
          lightness: 52
        }
      ]
    },{
      featureType: "administrative",
      elementType: "labels",
      stylers: [
        {
          hue: "#1100ff"
        },

        {
          saturation: -100
        },

        {
          lightness: -18
        }
      ]
    },{
      featureType: "poi",
      elementType: "all",
      stylers: [
        {
          lightness: -18
        },

        {
          visibility: "off"
        }
      ]
    },{
      featureType: "landscape",
      elementType: "all",
      stylers: [
        {
          visibility: "off"
        }
      ]
    },{
      featureType: "water",
      elementType: "labels",
      stylers: [
        {
          lightness: -18
        },

        {
          visibility: "off"
        }
      ]
    },{
      featureType: "road",
      elementType: "labels",
      stylers: [
        {
          visibility: "off"
        }
      ]
    },{
      featureType: "administrative",
      elementType: "geometry",
      stylers: [
        {
          visibility: "simplified"
        }
      ]
    },{
      featureType: "road.highway",
      elementType: "geometry",
      stylers: [
        {
          saturation: -100
        }
      ]
    },{
      featureType: "road.arterial",
      elementType: "geometry",
      stylers: [
        {
          saturation: -100
        }
      ]
    },{
      featureType: "road.local",
      elementType: "geometry",
      stylers: [
        {
          lightness: -27
        }
      ]
    },{
      featureType: "transit",
      elementType: "all",
      stylers: [
        {
          visibility: "off"
        }
      ]
    }
  ]

  var styledMapOptions = {
    name: "MapStyle1"
  }

  var jayzMapType = new google.maps.StyledMapType(
    stylez, styledMapOptions);


  lifemap.map.mapTypes.set('mapStyle1', jayzMapType)
  lifemap.map.setMapTypeId('mapStyle1')
  
}
