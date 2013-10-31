eclipse_smart_tab
=================

Do the right thing about the tab key in eclipse.

I started out with tab completion in bash and, like most people, wanted to move this capability to my code editor.
Having used emacs for years I gathered up a few useful TAB features:

* hippie expand
* block indent
* auto completion
* tab character insertion

But eclipse out of the box can only bind a single command to a key.

I created this very simple plugin to make eclipse do the right thing WRT TAB key when editing code.

Drop this plugin in your eclipse/plugins folder and when you press tab:

* if there is highlighted (selected) text, call Shift Right
* if there is whitespace to the left of your cursor insert a tab character "\t" (does not yet properly resolve spaces vs. tabs)
* if there is an object delimiter (currently ".", ">" and ":") call the auto completion (Content Assist)
* if there is any other character, call hippie expand

Future versions will likely resolve the tabs vs. spaces thing and object delimiters to work in more languages.

Pull requests gladly accepted.
