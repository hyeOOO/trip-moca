const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 5173,
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
  // ESLint 설정 수정
  lintOnSave: false,  // ESLint 검사 비활성화
  configureWebpack: {
    resolve: {
      fallback: {
        fs: false,
        path: false
      }
    }
  }
});