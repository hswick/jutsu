* Jutsu 術

** Currently in a stage of hammock driven development

Useful cljs clj app configuring
https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md

Would like to create the UI as FSM demonstrated here
http://blog.cognitect.com/blog/2017/5/22/restate-your-ui-using-state-machines-to-simplify-user-interface-development

* Sente example app
** What is this?

The [[https://github.com/ptaoussanis/sente/tree/master/example-project][example app]] from Sente, but with Boot instead of Leiningen.

** Usage

Fire the Boot pipeline from the command line.
#+BEGIN_SRC shell
$ boot dev
#+END_SRC

1. Connect to the REPL. 
2. Type ~(start!)~ in the REPL.
3. Your browser should automatically open to the provided port on localhost
4. Press Ctrl-C to exit logging and enter the REPl again
5. Type ~(test-fast-server>user-pushes)~ in the REPL. And look at output of js console
** More
Please also check out [[https://github.com/danielsz/sente-system][the example app for system]].