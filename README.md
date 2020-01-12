# hackerpaper

Convert a HackerNews thread to a more diff-able format (YAML).

If you used hackerpaper to convert an HN thread to YAML yesterday, and again
today, you have something that's much easier to find new comments with.

I'm not sure if the output is 100% valid YAML.


## Usage

Build the jar:

    $ lein uberjar

Convert an HN thread to YAML:

    $ java -jar target/uberjar/hackerpaper-0.1.0-standalone.jar 'https://news.ycombinator.com/item?id=21771743' > yesterday.yaml

Do it again later:

    $ java -jar target/uberjar/hackerpaper-0.1.0-standalone.jar 'https://news.ycombinator.com/item?id=21771743' > today.yaml

View differences (new comments) with Neovim:

    $ nvim -d yesterday.yaml today.yaml


## Nifty parts

In order to extract the necessary bits of HTML from the page, the HTML is
converted to a [Hiccup](http://github.com/weavejester/hiccup)-like structure
with [Hickory](https://github.com/davidsantiago/hickory). Why? Because I like
the Hiccup-"encoding" of HTML elements. No other reason.

Hickory has nice [CSS-style selectors](https://github.com/davidsantiago/hickory#selectors)
to use on its own data structure, but nothing like that for Hiccup-type
structures.

So I wrote some. It just turns out that they work quite well with threading
operators. Have a look at
[hiccup_tools.clj](./src/hackerpaper/hiccup_tools.clj). It's used all over the
place, but the best demonstration of those functions are probably in
`hackerpaper.core/parse`.

It should be easy enough to use copy and use that file in other projects.


## TODO

* [ ] Extract Hiccup-surgery tools into separate library.
* [ ] Extend `hackerpaper.hnthread/block-text` to convert italics and links to
  Markdown.
* [x] Support predicates for attribute values.


## License

[GPLv3](./LICENSE.md)
