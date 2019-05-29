# Developing

Run the following commands in the current directory:

```shell
yarn
yarn shadow-cljs watch dev
```

This will start up a compiler for ClojureScript.  Symlinking this directory under `~/.atom/packages/` is convenient for development.

Please note that Adorn activates via its "inline-def" command.

Once Adorn is activated, connect to a cljs repl by:

```shell
yarn shadow-cljs cljs-repl dev
```

Atom's devtools console may be helpful from time to time as well.
