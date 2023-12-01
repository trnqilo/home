#!/usr/bin/env node

const shell = require('child_process').execSync, express = require('express')().use(require('body-parser').json()), fs = require('fs')
const debouncers = new Set(), debounce = d => d && debouncers.add(d), undebounce = d => d && debouncers.delete(d), debounced = d => d && debouncers.has(d)
const sleep = ms => new Promise(wake => setTimeout(wake, ms)), log = (statusCode, req) => { console.log(`${new Date().toLocaleTimeString()} ${statusCode} ${req.method} ${req.originalUrl}`) }
const serverFile = process.argv[2], server = fs.existsSync(serverFile) ? JSON.parse(fs.readFileSync(serverFile, 'utf8')) : {}, root = server.root ?? '', port = server.port ?? 1234
for (const api of server.apis ?? [])
  express[(api.method ?? 'get').toLowerCase()](`${root}${api.route}` ?? '/', async (req, res) => {
    if (!api.debouncer || !debounced(api.debouncer)) {
      const statusCode = api.status ?? 200
      log(statusCode, req)
      debounce(api.debouncer)
      api.delay && await sleep(api.delay)
      api.shell && shell(api.shell)
      api.eval && eval(api.eval)
      api.import && import(`${process.cwd()}/${api.import}`)
      res.status(statusCode).send(api.response ?? { params: req.params ?? {}, query: req.query ?? {}, body: req.body ?? {} })
      undebounce(api.debouncer)
    } else {
      res.status(204).end()
      log('debounced', req)
    }
  })
express.all('*', function (req, res) {
  log(404, req)
  res.status(404).end()
})
express.listen(port, () => console.log(`listening on ${port}`))
