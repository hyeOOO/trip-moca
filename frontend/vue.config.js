const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true
})
//webpack 설정을 추가
module.exports = {
  css : {
    loaderOptions : {
      sass : {
        additionalData: `
          @import "@/assets/scss/abstracts/abstracts.scss";
        `
      }
    }
  }
}
