((emacs-lisp-mode
  (indent-tabs-mode . nil)
  (fill-column . 80)
  (sentence-end-double-space . t))
 (clojure-mode
  (clojure-indent-style . :always-align)
  (cider-ns-refresh-before-fn . "dev-extras/suspend")
  (cider-ns-refresh-after-fn  . "dev-extras/resume")
  (cider-repl-init-code . ("(dev)"))
  (cider-clojure-cli-global-options . "-A:dev:build:dev/build")))
