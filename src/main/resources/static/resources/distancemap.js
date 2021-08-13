let canvas;
let myMap;

let progress;
let progressCoordinates;

let options;

const mappa = new Mappa('Leaflet');

function preload() {
  progress = loadJSON('/data/distance/progress')
  options = loadJSON('/data/distance/options')
}

function setup() {
  canvas = createCanvas(windowWidth, windowHeight)
  myMap = mappa.tileMap(options)
  myMap.overlay(canvas, function() {
    myMap.map.scrollWheelZoom.disable()
  })
  progressCoordinates = Object.values(progress).map(obj => {
    if (obj.second) return {first: obj.first, second: myMap.geoJSON(obj.second, "LineString").flatMap(trip => trip)}
    else return null
  }).filter(o => o)

  myMap.onChange(drawPoints)
}

function drawPoints(){
  clear()
  noStroke()

  progressCoordinates.forEach(element => {
    fill(element.first.red, element.first.green, element.first.blue, 255)
    element.second.map(coord => myMap.latLngToPixel(coord.lat, coord.lng)).forEach(pos => {
      ellipse(pos.x, pos.y, 5, 5);
    })
  })
}
