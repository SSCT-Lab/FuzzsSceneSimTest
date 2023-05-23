const Mock = require('mockjs')
const fs = require('fs')
const data = Mock.mock({
  'items|30': [{
    id: '@id',
    title: '@sentence(10, 20)',
    'status|1': ['published', 'draft', 'deleted'],
    author: 'name',
    display_time: '@datetime',
    pageviews: '@integer(300, 5000)'
  }]
})

let seed1_content = ''
// D:\IDEA_Workspace\vue-admin-template\public\seeds\seed_0_0_1.xosc
fs.readFile('D:\\IDEA_Workspace\\vue-admin-template\\public\\seeds\\seed_0_0_1.xosc', (err, data) => {
  if (err) {
    console.log('readLocalFile error')
  } else {
    seed1_content = data.toString()
  }
})

module.exports = [
  {
    url: '/vue-admin-template/table/list',
    type: 'get',
    response: config => {
      const items = data.items
      return {
        code: 20000,
        data: {
          total: items.length,
          items: items
        }
      }
    }
  },
  {
    url: '/vue-admin-template/fileContent',
    type: 'get',
    response: config => {
      return {
        code: 20000,
        data: {
          items: seed1_content.toString()
        }
      }
    }
  }
]
