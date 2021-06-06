var qd = {};
if (location.search) location.search.substr(1).split("&").forEach(function(item) {
    var s = item.split("="),
        k = s[0],
        v = s[1] && decodeURIComponent(s[1]); //  null-coalescing / short-circuit
    //(k in qd) ? qd[k].push(v) : qd[k] = [v]
    (qd[k] = qd[k] || []).push(v) // null-coalescing / short-circuit
});

let canvas;
let myMap;

let data;
let routeCoordinates;

let progress;
let progressCoordinates;

const individualOptions = {
  lat: 32.04526885,
  lng: -80.1408972,
  zoom: 6,
  style: 'http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png'
}
const totalOptions = {
  lat: 33.0194843,
  lng: -80.0505926,
  zoom: 8,
  style: 'http://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png'
}
const mappa = new Mappa('Leaflet');

function preload() {
  if (qd.type == "individual") {
    data = loadJSON('/data/individual/route');
    progress = loadJSON('/data/individual/progress');
  } else if (qd.type == "total") {
    data = loadJSON('/data/total/route');
    progress = loadJSON('/data/total/progress');
  }
}

function setup() {
  canvas = createCanvas(windowWidth, windowHeight);
  if (qd.type == "individual") {
    myMap = mappa.tileMap(individualOptions);

  } else if (qd.type == "total") {
    myMap = mappa.tileMap(totalOptions)
  }
  myMap.overlay(canvas, function() {
    myMap.map.scrollWheelZoom.disable();
  });
  routeCoordinates = myMap.geoJSON(data, "LineString").flatMap(trip => trip);
  progressCoordinates = myMap.geoJSON(progress, "LineString").flatMap(trip => trip);

  myMap.onChange(drawPoints);
}

function drawPoints(){
  clear()
  noStroke();
  fill(255);
  routeCoordinates.map(coord => myMap.latLngToPixel(coord.lat, coord.lng)).forEach(pos => {
    ellipse(pos.x, pos.y, 5, 5);
  });
  fill(0, 255, 0);
  progressCoordinates.map(coord => myMap.latLngToPixel(coord.lat, coord.lng)).forEach(pos => {
    ellipse(pos.x, pos.y, 5, 5);
  });

  fill(255, 127, 0);
  let start = progressCoordinates[0];
  let startPos = myMap.latLngToPixel(start.lat, start.lng);
  ellipse(startPos.x, startPos.y, 7, 7);
}
