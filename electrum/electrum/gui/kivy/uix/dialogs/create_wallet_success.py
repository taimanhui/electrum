from kivy.app import App
from kivy.factory import Factory
from kivy.properties import ObjectProperty
from kivy.lang import Builder
from electrum.util import send_exception_to_crash_reporter, parse_URI, InvalidBitcoinURI
from electrum.paymentrequest import PR_UNPAID, PR_PAID, PR_UNKNOWN, PR_EXPIRED
from electrum.wallet import InternalAddressCorruption
from kivy.clock import Clock
from electrum.util import base_units_list
from electrum.i18n import languages
from electrum.gui.kivy.i18n import _
from electrum.plugin import run_hook
from electrum import coinchooser

#from electrum.gui.kivy.nfc_scanner.scanner_android import *

from .choice_dialog import ChoiceDialog
from electrum.gui.kivy.nfc_scanner.scanner_android import ScannerAndroid, scan

Builder.load_string('''
<CreateWalletSuccDialog@ModalView>
    id: succwallet
    address:''
    status: ''
    FloatLayout:
        orientation: 'vertical'
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 0.972, 0.976, 0.988, 1
            Rectangle:
                size: self.size
                pos: self.pos
        
        HeadModel:
            message: _('Create wallet success')
            action: root.dismiss
        
        FloatLayout:
            id: bl
            orientation: 'vertical'
            pos_hint: {'x':.05, 'top':.9}
            size_hint: .9, .8
            canvas.before:
                Color:
                    rgba: 1, 1, 1, 1
                Rectangle:
                    size: self.size
                    pos: self.pos
            Label:
                text:_('Create wallet success')
                pos_hint: {'x':.15, 'top':.98}
                size_hint: .7, .1
                font_size: 20
                color:  [0, 0, 0, 1]
                text_size: self.width, None
            Label:
                text:_('Create wallet success,Your payment address is as shown')
                pos_hint: {'x':.15, 'top':.9}
                size_hint: .7, .05
                font_size: 15
                color:  [0, 0, 0, 1]
                text_size: self.width, None
            QRCodeWidget:
                id: qr
                size_hint: .6, .6
                pos_hint: {'x':.2, 'top':.83}
                shaded: False
                #foreground_color: (0, 0, 0, 0.5) if self.shaded else (0, 0, 0, 0)
            Label:
                id:add
                text:''
                pos_hint: {'x':.15, 'top':.25}
                size_hint: 1, .1
                color:  [0, 0, 0, 1]
                font_size: 15
                text_size: self.width, None
            Button:
                text:_("Copy")
                pos_hint: {'x':.4, 'top':.18}
                font_size: 20
                size_hint: .2, .1
                color:[0, 0, 1, 1]
                font_size: 15
                background_normal: ''
                background_color: [0, 0, 0, .4] if self.state == 'down' else [1, 1, 1, .9]
                on_release:
                    Clock.schedule_once(partial(root.do_copy(), self)) 
            Label:
                text:_('BTC address, other currency will lost')
                pos_hint: {'x':.15, 'top':.1}
                size_hint: 1, .1
                color:  [1, 0.54, 0, 1]
                font_size: 12.5
                text_size: self.width, None
        Button:
            text:_("enter wallet")
            pos_hint: {'x':.15, 'y':.02}
            size_hint: .7, .07
            background_normal: ''
            background_color: [0, 0, 0, .4] if self.state == 'down' else [0.254, 0.411, 1, .9]
            background_normal: '../../theming/light/card_btn.png'
            background_down: '../../theming/light/card_btn.png'
            on_release:
                succwallet.dismiss()
                Clock.schedule_once(partial(root.enter_wallet, self))    
''')

class CreateWalletSuccDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.active = False
        self.address = ''
        #self.ids.add.text = self.address
        Factory.ModalView.__init__(self)
        self.update()

    def enter_wallet(self,item, bt):
        self.app.close_open_dialog()
        #return True

    def update(self):
        if not self.address:
            self.get_new_address()
        from electrum.util import create_bip21_uri
        uri = create_bip21_uri(self.address, '', '')
        qr = self.ids.qr
        qr.set_data(uri)

    def get_new_address(self) -> bool:
        """Sets the address field, and returns whether the set address
        is unused."""
        if not self.app.wallet:
            return False
        #self.clear()
        unused = True
        try:
            addr = self.app.wallet.get_unused_address()
            if addr is None:
                addr = self.app.wallet.get_receiving_address() or ''
                unused = False
        except InternalAddressCorruption as e:
            addr = ''
            self.app.show_error(str(e))
            send_exception_to_crash_reporter(e)
        self.address = addr
        self.ids.add.text = self.address
        return unused

