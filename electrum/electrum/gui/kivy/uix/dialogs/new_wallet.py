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
<ButtonTemp@Button>
    text:''
    pos_hint:''
    font_size: 20
    size_hint: .7, .08
    background_normal: ''
    background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
    background_normal: '../../theming/light/card_btn.png'
    background_down: '../../theming/light/card_btn.png'
    on_release:
        Clock.schedule_once(self.action)
                
<NewWalletDialog@ModalView>
    id: newwallet
    FloatLayout:
        orientation: 'vertical'
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 1, 1, 1, 1
            Rectangle:
                size: self.size
                pos: self.pos
        FloatLayout:
            orientation: 'vertical'
            pos_hint: {'x':.44, 'y':.62}
            size_hint: None, None
            size: 88, 88
            canvas.before:
                Color:
                    rgba: 0, 0, 0, 1
                BorderImage:
                    source: 'atlas://electrum/gui/kivy/theming/light/close'
                    size: self.width, self.height
                    pos: self.pos
        ButtonTemp:
            text:_("create wallet")
            pos_hint: {'x':.15, 'y':.3}
            disabled: not cb.active
            action: lambda x: app.popup_dialog('create_multi_wallet')
        ButtonTemp:
            text:_("import wallet")
            pos_hint: {'x':.15, 'y':.2}
            disabled: not cb.active
            action: lambda x: app.popup_dialog('create_multi_wallet')
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.1, 'y':.04}
            size_hint: 1, 0.1
            CheckBox:
                id:cb 
                size_hint: .5, 1
            Label:
                text: _('I have read and agreed to the agreement')
                size_hint: .5, 1
                text_size: self.width+dp(350), self.height-dp(40)
                color:  [.701, .701, .701, 1] 
                halign: 'left'
            
''')

class NewWalletDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        #self.active = False
        Factory.ModalView.__init__(self)

