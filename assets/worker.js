let port

let counter = 0

self.addEventListener("message", function(event) {
  console.log("::IN SERVICE WORKER::")

  const { data, } = event
  const { welcome, } = data

  if (welcome && event.ports[0]) {
    port = event.ports[0]
  }

  if (port) {
    port.postMessage({ I: "know you", randomThingy: Math.random(), })
  }
})

setInterval(() => {
  if (port) {
    port.postMessage({ I: "know you", counter: counter++, })
  }
}, 1000)
