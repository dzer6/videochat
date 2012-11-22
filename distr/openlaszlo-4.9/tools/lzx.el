;; lzx.el
;;

;; * E_LZ_COPYRIGHT_BEGIN ******************************************************
;; * Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.            *
;; * Use is subject to license terms.                                          *
;; * E_LZ_COPYRIGHT_END ********************************************************

;;; Description:

;; This file tells emacs to recognize *.lzx files as XML files.  With
;; a DTD (as described in the developer's guide), this will provide
;; syntax-directed editing and validation of XML entities in LZX
;; files.

;; If mmm-mode is installed, this file will also create an mmm submode
;; to recognize the content of <method> and <script> tags as
;; Javascript, and direct mmm to use this submode for *.lzx files.
;; This provides syntax coloring and intelligent indentation and
;; navigation for Javascript code within LZX files.

;;; Installation:

;; Copy this file into a directory on the load-path, optionally
;; byte-compile it (using M-x byte-compile-file), and place the
;; following lines into your .emacs:
;;
;;      (add-to-list 'load-path "path/to/mmm-mode-0.4.7")
;;      (load-library "mmm-mode")
;;      (require 'mmm-mode)
;;	(require 'lzx)
;;
;; (If you don't wish to use mmm mode, only the last line is
;; required.)
;;
;; If you want mmm mode to be invoked automatically when you open a file, add:
;;
;;      (mmm-add-find-file-hook)
;;
;; to your .emacs file.

(provide 'lzx)

(when (fboundp 'nxml-mode)
  (setq auto-mode-alist
	(append '(("\\.lz[lx]$" . nxml-mode))
                auto-mode-alist)))

(when (fboundp 'mmm-mode)
  (mmm-add-group
   'lzx
   '(
     (js-method-cdata
      :submode javascript
      :face mmm-code-submode-face
      :front "<method[^>]*>[ \t\n]*<!\\[CDATA\\[[ \t]*\n?"
      :back "[ \t]*]]>[ \t\n]*</method>"
      :insert (((meta . ?m) js-method-cdata "name: " @ "<method name=\"" str "\">\n<![CDATA["
		   @ "\n" _ "\n" @ "]]></method>" @))
      )
     (js-method
      :submode javascript
      :face mmm-code-submode-face
      :front "<method[^>]*>[ \t]*\n?"
      :back "[ \t]*</method>"
      :insert ((?m js-method "name: " @ "<method name=\"" str "\">"
		   @ "\n" _ "\n" @ "</method>" @))
      )
     (js-script-cdata
      :submode javascript
      :face mmm-code-submode-face
      :front "<script[^>]*>[ \t\n]*<!\\[CDATA\\[[ \t]*\n?"
      :back "[ \t]*]]>[ \t\n]*</script>"
      :insert (((meta . ?s) js-script-cdata nil @ "<script>\n<![CDATA["
		   @ "\n" _ "\n" @ "]]>\n</script>" @))
      )
     (js-script
      :submode javascript
      :face mmm-code-submode-face
      :front "<script[^>]*>[ \t]*\n?"
      :back "[ \t]*</script>"
      :insert ((?s js-script nil @ "<script>"
		   @ "\n" _ "\n" @ "</script>" @))
      )
     (js-inline
      :submode javascript
      :face mmm-code-submode-face
      :front "on\w+=\""
      :back "\"")))

  (mmm-add-mode-ext-class 'nxml-mode "\\.lz[lx]\\'" 'lzx)
  )
