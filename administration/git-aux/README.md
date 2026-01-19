# Content

scripts in dirs:
  * ./ - final script doing some complete work, must be run from this dir:
    * *.sh (without postfix "-win") - origin scripts, see help inside in the begining
    * *-win.sh - derived scripts, they run origin scripts in the GUI-window, in the end wait for key Enter to finish,
                 suitable for runnig by "click" or from the Desktop as "shortcut"
    specially:
    * git-config-set.sh - recomnended git-config
    * git-fix.sh - auxiliary commands to fix repo in not-up-to-date or not-clean status
    * git-status-*.sh - to diagnostic status of repo
  * base/ - basic scripts, complete works are compounded from this basic scripts, must by run from project-root-dir
    * env.sh - settings of shared parameters for all scripts
