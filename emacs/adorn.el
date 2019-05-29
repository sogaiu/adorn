;;; adorn.el --- Wrapper for adorn  -*- lexical-binding: t; -*-

;; Author: sogaiu
;; URL: https://github.com/sogaiu/adorn/emacs
;; Version: 0.1-pre
;; Package-Requires: ((emacs "25.2"))
;; Keywords: instrumentation

;; This file is not part of GNU Emacs.

;;; Commentary:

;; 

;;;; Installation

;;;;; Manual

;; Put this file in your load-path, and put this in your init file:

;; (require 'adorn)

;;;; Usage

;; With point in an appropriate Clojure form, run one of the commands:

;; `adorn-inline-def': try to add inline-defs to a Clojure defn / defn-

;;;; Issues

;; `adorn-inline-def' modifies where point is -- is this appropriate?

;; `adorn-temp-output' is stateful -- is it reset appropriately?

;; `adorn-path' computes the path to the adorn binary -- what if this fails?

;;; Code:

;;;; Requirements

(require 'files)
(require 'json)
(require 'simple)

;;;;; Variables

;; XXX: should be some kind of local thing?
(defvar adorn-temp-output
  '()
  "List to accumulate output from adorn process.")

;;;;; Commands

(defun adorn-path ()
  "Determine path to adorn binary."
  (concat (locate-dominating-file
           (symbol-file 'adorn-temp-output)
           "adorn.el")
          "bin/adorn"))

(defun adorn-filter (process output)
  "Filter for processing adorn command output."
  (setq adorn-temp-output
        (cons output adorn-temp-output)))

(defun adorn-reset-temp-output ()
  (setq adorn-temp-output '()))

;; based on:
;;   https://emacs.stackexchange.com/a/8083
(defun adorn-pos-at-row-col (row col)
  "Determine buffer position for ROW, COL.

ROW and COL are zero-based, i.e. 0, 0 is the beginning of the buffer."
  (save-excursion
    (goto-char (point-min))
    (forward-line row)
    (move-to-column col)
    (point)))

;; handles processing complete output from adorn process
(defun adorn-sentinel (process event)
  "Sentinel for adorn process."
  (when (equal "finished\n" event)
    (let* ((output-str (apply 'concat adorn-temp-output))
           (parsed (adorn-from-json-rpc-str output-str))
           (result (cdr (assoc 'result parsed)))
           (new-text (elt result 0))
           (range (elt result 1)))
      (if (vectorp range)
        ;; XXX: want destructuring bind
        (let* ((start-pair (elt range 0))
               (start-row (elt start-pair 0))
               (start-col (elt start-pair 1))
               (start (adorn-pos-at-row-col (1- start-row) (1- start-col)))
               (end-pair (elt range 1))
               (end-row (elt end-pair 0))
               (end-col (elt end-pair 1))
               (end (adorn-pos-at-row-col (1- end-row) (1- end-col))))
          (delete-region start end)
          (insert new-text))
        (message "adorn didn't change anything")))
    (adorn-reset-temp-output)))

(defun adorn-to-json-rpc-str (method params id)
  "Create JSON RPC string from METHOD, PARAMS, and ID."
  (json-encode (list (cons "jsonrpc" "2.0")
                     (cons "method" method)
                     (cons "params" params)
                     (cons "id" id))))

(defun adorn-from-json-rpc-str (json-rpc-str)
  "Parse JSON-RPC-STR."
  (json-read-from-string json-rpc-str))

(defun adorn-get-buffer-text ()
  (buffer-substring-no-properties 1 (1+ (buffer-size))))

;;;###autoload
(defun adorn-inline-def ()
  "Apply the inline-def transformation to a defn surrounding point."
  (interactive)
  (condition-case err
      (let* ((json-rpc-str
              (adorn-to-json-rpc-str "inlinedef"
                                     (list (adorn-get-buffer-text)
                                           (line-number-at-pos)
                                           (1+ (current-column)))
                                     1))
             (adorn-proc (make-process :name "adorn"
                                       :buffer nil
                                       :command (list (adorn-path))
                                       :connection-type 'pipe
                                       :filter 'adorn-filter
                                       :sentinel 'adorn-sentinel)))
        (when adorn-proc
          ;; XXX: is this sufficient?
          (adorn-reset-temp-output)
          (process-send-string adorn-proc json-rpc-str)
          (process-send-eof adorn-proc)))
    (error
     (message "Error: %s %s" (car err) (cdr err)))))

;;;; Footer

(provide 'adorn)

;;; adorn.el ends here

;; (setq test-str
;;       (adorn-to-json-rpc-str "inlinedef"
;;                              '("(defn my-fn [a] (+ a 1))" 1 5)
;;                              1))

;; (setq adorn-proc 
;;       (make-process :name "adorn"
;;                     :command (list (adorn-path))
;;                     :connection-type 'pipe
;;                     :filter 'adorn-filter
;;                     :sentinel 'adorn-sentinel))

;; (process-send-string adorn-proc
;;                      test-str)

;; (process-send-eof adorn-proc)
