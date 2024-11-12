const { defineConfig } = require('@vue/cli-service');
module.exports = defineConfig({
  transpileDependencies: true,
});
//webpack 설정을 추가
module.exports = {
  devServer: {
    port: 5173, // 원하는 포트 번호
  },
  css: {
    loaderOptions: {
      sass: {
        additionalData: `
          @import "@/assets/scss/abstracts/abstracts.scss";
        `,
      },
    },
  },
};
