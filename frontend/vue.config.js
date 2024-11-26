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
      // stylus 로더 옵션 추가
      stylus: {
        additionalData: `
          @import "@/assets/stylus/variables.styl"
        `
      }
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
    },
    module: {
      rules: [
        {
          test: /\.pug$/,
          use: ['pug-plain-loader']
        }
      ]
    }
  },
  chainWebpack: config => {
    // Pug Loader
    config.module
      .rule('pug')
      .test(/\.pug$/)
      .use('pug-plain-loader')
      .loader('pug-plain-loader')
      .end()
  }
});