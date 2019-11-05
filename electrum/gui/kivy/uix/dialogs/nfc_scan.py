from kivy.app import App
from kivy.factory import Factory
from kivy.properties import ObjectProperty
from kivy.lang import Builder

from electrum.util import base_units_list
from electrum.i18n import languages
from electrum.gui.kivy.i18n import _
from electrum.plugin import run_hook
from electrum import coinchooser

#from electrum.gui.kivy.nfc_scanner.scanner_android import *

from .choice_dialog import ChoiceDialog
from electrum.gui.kivy.nfc_scanner.scanner_android import ScannerAndroid, scan

Builder.load_string('''
<NfcScanDialog@ModalView>
    id: nfc
    FloatLayout:
        orientation: 'vertical'
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 1, 1, 1, 1
            Rectangle:
                size: self.size
                pos: self.pos
        Button:
            pos_hint: {'x':.02, 'y':.92}
            size_hint: None, None
            size: 50, 30
            on_release: root.dismiss()
            background_normal: ''
            background_color: [1, 1, 1, 1]
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/arrow_back'
                y: self.parent.y
                x: self.parent.x
                size: 50, 30
                allow_stretch: True
        Label:
            pos_hint: {'x':.3, 'y':.77}
            size_hint: .4, .1
            text: _('please touch your card')
            color:  [0, 0, 0, 1]
            font_size: 25
        FloatLayout:
            orientation: 'vertical'
            pos_hint: {'x':.2, 'y':.22}
            size_hint: .6, .5
            canvas.before:
                Color:
                    rgba: 0.254, 0.411, 1, 1
                BorderImage:
                    source: 'atlas://electrum/gui/kivy/theming/light/card_btn'
                    size: self.width, self.height
                    pos: self.pos
''')

class NfcScanDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.plugins = self.app.plugins
        self.config = self.app.electrum_config
        Factory.ModalView.__init__(self)
        scan.nfc_init(self.get_card_status)
        scan.nfc_enable()

    def on_open(self):
        print("on_open")
        # import time
        # time.sleep(2)
        # self.get_card_status()

    def get_card_status(self):
        #get status from smart card
        exist = scan.get_card_info()
        if exist[0] == 0:
            #activate smart card
            self.app.password_dialog(self.app.wallet, _('New Password'), self.get_pw, self.stop_nfc, is_change=3)
        else:
            print("Already activated,popup have been activated dialog")
            #Already activated,popup have been activated dialog
            #self.app.popup_dialog('')

    def stop_nfc(self):
        print("stop_nfc")
        #scan.nfc_disable()

    def get_pw(self, pw):
        success = False
        #if self.exist[0] == 0:
        scan.init_card(pw)
'''
        if self.exist[0] == 0:
            #scan.init_card(pw)
            success = True
        else:
            verifyResult = scan.verify_password(pw)
            if verifyResult[0] == 0x01:
                success = True
            else:
                self.verify_password_limit(verifyResult[1])
        if success:
            self._scan_nfc()
'''