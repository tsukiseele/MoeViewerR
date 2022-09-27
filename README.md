
### 该项目正在使用Flutter重写以支持更多平台，请参见[ComicNyaa](https://github.com/tsukiseele/ComicNyaa).
### The project is being rewritten using Flutter to support more platforms, see [ComicNyaa](https://github.com/tsukiseele/ComicNyaa).

<img src="https://cdn.jsdelivr.net/gh/tsukiseele/MoeViewerR/sample/ic_launcher-web.png" width="19%" alt="MoeViewerR Icon"/>

# MoeViewerR
插件订阅式的漫画阅读应用，支持Exhentai，以及Yande、Gelbooru、SankakuComplex等主流Booru图库。

该软件通过加载插件运行，具体的内容取决于插件本身，与本软件无关。

为方便使用，本软件默认包含如下站点，标注 * 号意味着该网站在**特定国家/地区**可能404。

#### 漫画&同人志&图集
- E-Hentai-Lofi *
- E-Hentai *
- Exhentai *
- Konachan Pool
- Moeimg
- Mzitu
- N-Hentai
- Yande Pool *

#### 图库
- 3DBooru
- AtfBooru
- Chan SankakuComplex
- DanBooru
- E-ShuuShuu
- FootFetishBooru
- FurryBooru
- GelBooru
- GuroBooru
- HentaiBooru
- Idol SankakuComplex
- Konachan (R18)
- Konachan (全年龄)
- LoliBooru *
- MiniTokyo
- Rule34
- SafeBooru
- SakugaBooru
- Yande *
- YuriBooru
- Yuriimg
- Zerochan

## Requirement
- Android 5.1 (SDK 22) 或更高版本。
- 独立开发，缺少足够的测试，有问题欢迎提Issue，但因为工作原因，不一定有时间解决。

## Usage
- 首次打开会提示是否更新默认订阅，若更新失败或者没有数据，请添加如下订阅（建议复制）：
  ```
  https://cdn.jsdelivr.net/gh/tsukiseele/MoeViewerR/packs/default_package.zip 
  ```
- **注意：站点列表在右侧抽屉内，从屏幕右侧边缘往左滑即可打开。**
- 如果你觉得好用，不妨点个star。

## License
- 参见 [LICENSE](./LICENSE)

## Sample
<div align="center">
  <img src="https://cdn.jsdelivr.net/gh/tsukiseele/MoeViewerR/sample/sample_1.jpg" alt="sample 1" width="35%"/>
  <img src="https://cdn.jsdelivr.net/gh/tsukiseele/MoeViewerR/sample/sample_2.jpg" alt="sample 2" width="35%"/>
  <img src="https://cdn.jsdelivr.net/gh/tsukiseele/MoeViewerR/sample/sample_3.jpg" alt="sample 3" width="35%"/>
  <img src="https://cdn.jsdelivr.net/gh/tsukiseele/MoeViewerR/sample/sample_4.jpg" alt="sample 4" width="35%"/>
</div>

## Plugins
不推荐没有相关知识的用户编写插件，如有需要，请提Issue。

对于有需要的用户，提供简单的说明，建议在以下模板中修改，注释必须去掉（JSON不支持注释）
```
{
  "id": "唯一值即可",
  "version": "用于更新，一般填1",
  "name": "插件名，会在列表中显示",
  "author": "插件作者名，可选",
  "rating": "评级，全年龄：S，限制：R",
  "details": "插件简介",
  "flag": "标志位，不填",
  "type": "类型，用于分类，必填",
  "icon": "插件图标，必须是超链接",
  // 以下为抓取规则，sections对应站点的多个板块，如主页，搜索，推荐，等等
  "sections": {
    // 板块名
    "home": {
      // index为板块分页URL，可用以下模板：
      // {page: <offset>, <size>}
      // {keywords: <kwd>} 

      // 对于{page: <offset>, <size>}， offset为分页修正值，以1为初始值计算，可为负数，0代表不修正。
      // size为分页步距，可空，默认为1。若改为5，则每翻一页分页值都会间隔5。
      // 对于{keywords: <kwd>}， kwd为关键字，默认为空，若填写，则使用其作为默认值查询。
      "index": "https://yande.re/post?page={page:0}",
      "rules": [
        {
          // 标题
          "title": {
            "selector": "$(ul#post-list-posts > li).attr(id)",
            "capture": "(?<=p)(.*)",
            "replacement": "yande $1"
          },
          // 封面
          "coverUrl": {
            "selector": "$(div > a.thumb > img).attr(src)"
          },
          // 子项页面索引
          "$children": {
            "selector": "$(div.inner > a).attr(href)",
            "capture": "(.*)",
            "replacement": "https://yande.re/$1"
          }
        },
        {
          // 子项标签
          "tags": {
            "selector": "$(img#image).attr(alt)"
          },
          // 子项预览大图URL
          "sampleUrl": {
            "selector": "$(img#image).attr(src)"
          },
          // 子项大图URL
          "largerUrl": {
            "selector": "$(li > a#highres).attr(href)"
          },
          // 子项原图URL
          "originUrl": {
            "selector": "$(li > a#png).attr(href)"
          },
          // 发布时间
          "datetime": {
            "selector": "$(script#forum-posts-latest).html()",
            "capture": "(\\d{4}-\\d{2}-\\d{2})T(\\d{2}:\\d{2}:\\d{2})",
            "replacement": "$1 $2"
          }
        }
      ]
    },
    // 搜索板块
    "search": {
      // 分页索引，同上
      "index": "https://yande.re/post?page={page:0}&tags={keywords:}",
      // 重用规则，这里是重用了home的规则 
      "reuse": "home"
    }
  }
}

```
