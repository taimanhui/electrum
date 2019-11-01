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
<AddCosignerDialog@ModalView>
    id: addc
    FloatLayout:
        orientation: 'vertical'
        pos_hint: {'x':.05, 'y':0}
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 0.972, 0.976, 0.988, 1
            Rectangle:
                size: self.size
                pos: self.pos
        HeadModel:
            message: _('Add Cosigner') 
            action: root.dismiss
       
        Label:
            text:_('method one:use hardware')
            pos_hint: {'x':.05, 'y':.8}
            size_hint: .7, .1
            color:  [0.286, 0.286, 0.286, 1]
            #halign:'left'
            font_size: '15sp'
            text_size: self.width, None
        Button:
            text:_("Use hardware")
            pos_hint: {'x':.05, 'y':.7}
            font_size: 20
            size_hint: .9, .1
            background_normal: ''
            background_color: [0, 0, 0, .4] if self.state == 'down' else [0.254, 0.411, 1, .9]
            background_normal: '../../theming/light/card_btn.png'
            background_down: '../../theming/light/card_btn.png'
            on_release:
                #addc.dismiss()
                Clock.schedule_once(lambda x: app.wizard.restore_from_xpub(root.ids.xpub.text))   
        Label:
            text:_('method two:use xpub/tpub/vpub')
            pos_hint: {'x':.05, 'top':.65}
            size_hint: .7, .1
            color:  [0.286, 0.286, 0.286, 1]
            #halign:'left'
            font_size: '15sp'
            text_size: self.width, None
        TextInput:
            id: xpub
            text:_('please input extended public key')
            pos_hint: {'x':0.05, 'top':.55}
            foreground_color:(0.286, 0.286, 0.286, 1) if self.focus else [0.702, 0.702, 0.702, 1]
            background_normal: ''
            background_color:(0.972, 0.972, 0.972, 1)
            size_hint: .9, .2
        
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':0.05, 'top':.2}
            size_hint: .9, None
            height: '48dp'
            spacing: '5sp'
            IconButton:
                id: scan
                height: '48sp'
                on_release: root.scan_xpub()
                icon: 'atlas://electrum/gui/kivy/theming/light/camera'
                size_hint: 1, None
                background_normal: ''
                background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
            Button:
                text: _('Paste')
                on_release: root.do_paste()
                background_normal: ''
                background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
            Button:
                text: _('Clear')
                on_release: root.do_clear()
                background_normal: ''
                background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
            Button:
                text: _('OK')
                on_release: Clock.schedule_once(lambda x: app.wizard.restore_from_xpub(root.ids.xpub.text))
                background_normal: ''
                background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
        # Button:
        #     text:_("Next")
        #     pos_hint: {'x':.15, 'y':.2}
        #     font_size: 20
        #     size_hint: .7, .1
        #     background_normal: ''
        #     background_color: [0, 0, 0, .4] if self.state == 'down' else [0.254, 0.411, 1, .9]
        #     background_normal: '../../theming/light/card_btn.png'
        #     background_down: '../../theming/light/card_btn.png'
        #     on_release:
        #         #addc.dismiss()
        #         Clock.schedule_once(lambda x: app.wizard.restore_from_xpub(root.ids.xpub.text))
            
''')

class AddCosignerDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.active = False
#        self.plugins = self.app.plugins
#        self.config = self.app.electrum_config
        Factory.ModalView.__init__(self)

    def do_paste(self):
        self.ids.xpub.text = self.app._clipboard.paste()
        print("clipboard text = %s..." % self.ids.xpub.text)

    def do_clear(self):
        self.ids.xpub.text = ''

    def scan_xpub(self):
        def on_complete(text):
            #if self.allow_multi:
            #    self.ids.text_input.text += text + '\n'
            #else:
            self.ids.text_input.text = text
        self.app.scan_qr(on_complete)