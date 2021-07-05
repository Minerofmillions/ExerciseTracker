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

let options;

const mappa = new Mappa('Leaflet');

function preload() {
  data = loadJSON('/data/' + qd.type + '/route')
  progress = loadJSON('/data/' + qd.type + '/progress');
  options = loadJSON('/data/' + qd.type + '/options')
}

function setup() {
  canvas = createCanvas(windowWidth, windowHeight);
  myMap = mappa.tileMap(options);
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
  fill('rgba(127,127,127,0.25)');
  routeCoordinates.map(coord => myMap.latLngToPixel(coord.lat, coord.lng)).forEach(pos => {
    ellipse(pos.x, pos.y, 5, 5);
  });
  fill('rgba(0,255,0,0.25)');
  progressCoordinates.map(coord => myMap.latLngToPixel(coord.lat, coord.lng)).forEach(pos => {
    ellipse(pos.x, pos.y, 5, 5);
  });

  fill(255, 127, 0);
  let start = progressCoordinates[0];
  let startPos = myMap.latLngToPixel(start.lat, start.lng);
  ellipse(startPos.x, startPos.y, 7, 7);
}
