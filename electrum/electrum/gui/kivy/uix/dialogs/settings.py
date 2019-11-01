from kivy.app import App
from kivy.factory import Factory
from kivy.properties import ObjectProperty
from kivy.lang import Builder

from electrum.util import base_units_list
from electrum.i18n import languages
from electrum.gui.kivy.i18n import _
from electrum.plugin import run_hook
from electrum import coinchooser

from .choice_dialog import ChoiceDialog
from electrum.gui.kivy.nfc_scanner.scanner_android import ScannerAndroid, scan
"""
Builder.load_string('''
#:import partial functools.partial
#:import _ electrum.gui.kivy.i18n._

<SettingsDialog@ModalView>
    #:import VERSION electrum.version.ELECTRUM_VERSION

    id: nd
    font_cn: ''
        
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
            pos_hint: {'x':0, 'top':1}
            size_hint: 1, None
            height:'50dp'
            canvas.before:
                Color:
                    rgb: 1, 0.98, 0.941
                Rectangle:
                    size: self.size
                    pos: self.pos
            Button:
                pos_hint: {'x':0, 'top':1}
                size_hint: .1, None
                height:50
                on_release: root.dismiss()
                background_normal: ''
                background_color: [1, 0.98, 0.941, 1]
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/arrow_back'
                    y: self.parent.y+dp(5)
                    x: self.parent.x
                    size: 50, 30
                    allow_stretch: True
            Label:
                text: _('Setting')
                font_size: '18sp'
                pos_hint: {'center_x':.5, 'top':1}
                size_hint: .9, 1
                #height:'50dp'
                text_size: self.width, self.height-sp(25)
                color:  [0.098, 0.098, 0.439, 1]
                background_normal: ''
                background_color: 1, .585, .878, 0
                halign: 'center'
        
        
''')

"""
Builder.load_string('''
#:import partial functools.partial
#:import _ electrum.gui.kivy.i18n._

<SettingsDialog@ModalView>
    #:import VERSION electrum.version.ELECTRUM_VERSION

    id: nd
    font_cn: ''
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
            text: _('Setting')
            #bold: True
            font_size: '38sp'
            pos_hint: {'x':.02, 'y':.81}
            size_hint: .8, .1
            text_size: self.width - dp(40), self.height
            color:  [0.098, 0.098, 0.439, 1]
        Label:
            text: _('SUGGESTED SCENES')
            pos_hint: {'x':.02, 'y':.72}
            size_hint: .8, .1
            text_size: self.width - dp(50), self.height
            color:  [.745, .745, .745, 1]
        
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.02, 'y':.59}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
            BoxLayout:
                orientation: 'vertical'
                size_hint: None, None
                size: 30, 30
                #height: '30dp'
                canvas.before:
                    Color:
                        rgb: 0.098, 0.098, 0.439
                    BorderImage:
                        source: 'atlas://electrum/gui/kivy/theming/light/invalid_name'
                        size: self.width, self.height
                        pos: self.pos 
            Label:
                text_size: self.width-dp(20), self.height-dp(13 )
                size_hint: .7, None
                text: _("check/activation cold wallet")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [1, 1, 1, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.1, 'y':.58}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.02, 'y':.5}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
            BoxLayout:
                orientation: 'vertical'
                size_hint: None, None
                size: 30, 30
                height: '30dp'
                canvas.before:
                    Color:
                        rgb: 0.098, 0.098, 0.439
                    BorderImage:
                        source: 'atlas://electrum/gui/kivy/theming/light/invalid_name'
                        size: self.width, self.height
                        pos: self.pos 
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("message manager")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [1, 1, 1, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.1, 'y':.49}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.02, 'y':.41}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
            BoxLayout:
                orientation: 'vertical'
                size_hint: None, None
                size: 30, 30
                height: '30dp'
                canvas.before:
                    Color:
                        rgb: 0.098, 0.098, 0.439
                    BorderImage:
                        source: 'atlas://electrum/gui/kivy/theming/light/invalid_name'
                        size: self.width, self.height
                        pos: self.pos 
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("Language")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [1, 1, 1, 1]
                on_release:
                    Clock.schedule_once(partial(root.language_dialog, self))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.1, 'y':.4}
        Label:
            text: _('MORE')
            pos_hint: {'x':.02, 'y':.37}
            size_hint: 1, .1
            text_size: self.width - dp(50), self.height
            color:  [.745, .745, .745, 1] 
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.02, 'y':.24}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
            BoxLayout:
                orientation: 'vertical'
                size_hint: None, None
                size: 30, 30
                height: '30dp'
                canvas.before:
                    Color:
                        rgb: 0.098, 0.098, 0.439
                    BorderImage:
                        source: 'atlas://electrum/gui/kivy/theming/light/invalid_name'
                        size: self.width, self.height
                        pos: self.pos 
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("service online")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [1, 1, 1, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.1, 'y':.23}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.02, 'y':.15}
            size_hint: 1, None
            height: '40dp'
            padding: '30dp'
            BoxLayout:
                orientation: 'vertical'
                size_hint: None, None
                size: 30, 30
                height: '30dp'
                canvas.before:
                    Color:
                        rgb: 0.098, 0.098, 0.439
                    BorderImage:
                        source: 'atlas://electrum/gui/kivy/theming/light/invalid_name'
                        size: self.width, self.height
                        pos: self.pos 
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("About")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [1, 1, 1, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.1, 'y':.14}
''')

"""
Builder.load_string('''
#:import partial functools.partial
#:import _ electrum.gui.kivy.i18n._

<SettingsDialog@Popup>
    id: settings
    title: _('BiXin Settings')
    disable_pin: False
    use_encryption: False
    BoxLayout:
        orientation: 'vertical'
        ScrollView:
            GridLayout:
                id: scrollviewlayout
                cols:1
                size_hint: 1, None
                height: self.minimum_height
                padding: '10dp'
                SettingsItem:
                    lang: settings.get_language_name()
                    title: 'Language' + ': ' + str(self.lang)
                    description: _('Language')
                    action: partial(root.language_dialog, self)
                CardSeparator
                SettingsItem:
                    disabled: root.disable_pin
                    title: _('PIN code')
                    description: _("Change your PIN code.")
                    action: partial(root.change_password, self)
                CardSeparator
                SettingsItem:
                    bu: app.base_unit
                    title: _('Denomination') + ': ' + self.bu
                    description: _("Base unit for Bitcoin amounts.")
                    action: partial(root.unit_dialog, self)
                CardSeparator
                SettingsItem:
                    status: root.fx_status()
                    title: _('Fiat Currency') + ': ' + self.status
                    description: _("Display amounts in fiat currency.")
                    action: partial(root.fx_dialog, self)
                CardSeparator
                SettingsItem:
                    status: 'ON' if bool(app.plugins.get('labels')) else 'OFF'
                    title: _('Labels Sync') + ': ' + self.status
                    description: _("Save and synchronize your labels.")
                    action: partial(root.plugin_dialog, 'labels', self)
                CardSeparator
                SettingsItem:
                    status: 'ON' if app.use_rbf else 'OFF'
                    title: _('Replace-by-fee') + ': ' + self.status
                    description: _("Create replaceable transactions.")
                    message:
                        _('If you check this box, your transactions will be marked as non-final,') \
                        + ' ' + _('and you will have the possibility, while they are unconfirmed, to replace them with transactions that pays higher fees.') \
                        + ' ' + _('Note that some merchants do not accept non-final transactions until they are confirmed.')
                    action: partial(root.boolean_dialog, 'use_rbf', _('Replace by fee'), self.message)
                CardSeparator
                SettingsItem:
                    status: _('Yes') if app.use_unconfirmed else _('No')
                    title: _('Spend unconfirmed') + ': ' + self.status
                    description: _("Use unconfirmed coins in transactions.")
                    message: _('Spend unconfirmed coins')
                    action: partial(root.boolean_dialog, 'use_unconfirmed', _('Use unconfirmed'), self.message)
                CardSeparator
                SettingsItem:
                    status: _('Yes') if app.use_change else _('No')
                    title: _('Use change addresses') + ': ' + self.status
                    description: _("Send your change to separate addresses.")
                    message: _('Send excess coins to change addresses')
                    action: partial(root.boolean_dialog, 'use_change', _('Use change addresses'), self.message)
                CardSeparator
                SettingsItem:
                    title: _("Reset Password")
                    description: _("Reset password forsmart card")
                    action: lambda x: app.popup_dialog('resetpin')
                CardSeparator
                SettingsItem:
                    title: _("Unlock Password")
                    description: _("Unlock password for smart card")
                    action: lambda x: app.popup_dialog('unlockpin')

                # disabled: there is currently only one coin selection policy
                #CardSeparator
                #SettingsItem:
                #    status: root.coinselect_status()
                #    title: _('Coin selection') + ': ' + self.status
                #    description: "Coin selection method"
                #    action: partial(root.coinselect_dialog, self)
''')
"""


class SettingsDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.plugins = self.app.plugins
        self.config = self.app.electrum_config
        Factory.ModalView.__init__(self)
        #self.font_cn = self.app.font_cn
        #print("self.app.font_cn = %s..." % self.font_cn)
        #layout = self.ids.setviewlayout
        #layout.bind(minimum_height=layout.setter('height'))
        # cached dialogs
        # self._fx_dialog = None
        # self._proxy_dialog = None
        self._language_dialog = None
        # self._unit_dialog = None
        # self._coinselect_dialog = None

    def language_dialog(self, item, dt):
        print("language_dialog in....")
        if self._language_dialog is None:
            print("languate_dialog == None")
            l = self.config.get('language', 'en_UK')

            def cb(key):
                print("key %s++++" %key)
                self.config.set_key("language", key, True)
                item.lang = self.get_language_name()
                self.app.language = key

            self._language_dialog = ChoiceDialog(_('Language'), languages, l, cb)
        print("language test....")
        self._language_dialog.open()

    def get_language_name(self):
        return languages.get(self.config.get('language', 'en_UK'), '')
'''
    def update(self):
        #self.wallet = self.app.wallet
        #self.disable_pin = self.wallet.is_watching_only() if self.wallet else True
        #self.use_encryption = self.wallet.has_password() if self.wallet else False

    

    def change_password(self, item, dt):
        self.app.change_password(self.update)

    

    def unit_dialog(self, item, dt):
        if self._unit_dialog is None:
            def cb(text):
                self.app._set_bu(text)
                item.bu = self.app.base_unit
            self._unit_dialog = ChoiceDialog(_('Denomination'), base_units_list,
                                             self.app.base_unit, cb, keep_choice_order=True)
        self._unit_dialog.open()

    def coinselect_status(self):
        return coinchooser.get_name(self.app.electrum_config)

    def coinselect_dialog(self, item, dt):
        if self._coinselect_dialog is None:
            choosers = sorted(coinchooser.COIN_CHOOSERS.keys())
            chooser_name = coinchooser.get_name(self.config)
            def cb(text):
                self.config.set_key('coin_chooser', text)
                item.status = text
            self._coinselect_dialog = ChoiceDialog(_('Coin selection'), choosers, chooser_name, cb)
        self._coinselect_dialog.open()

    def proxy_status(self):
        net_params = self.app.network.get_parameters()
        proxy = net_params.proxy
        return proxy.get('host') +':' + proxy.get('port') if proxy else _('None')

    def proxy_dialog(self, item, dt):
        network = self.app.network
        if self._proxy_dialog is None:
            net_params = network.get_parameters()
            proxy = net_params.proxy
            def callback(popup):
                nonlocal net_params
                if popup.ids.mode.text != 'None':
                    proxy = {
                        'mode':popup.ids.mode.text,
                        'host':popup.ids.host.text,
                        'port':popup.ids.port.text,
                        'user':popup.ids.user.text,
                        'password':popup.ids.password.text
                    }
                else:
                    proxy = None
                net_params = net_params._replace(proxy=proxy)
                network.run_from_another_thread(network.set_parameters(net_params))
                item.status = self.proxy_status()
            popup = Builder.load_file('electrum/gui/kivy/uix/ui_screens/proxy.kv')
            popup.ids.mode.text = proxy.get('mode') if proxy else 'None'
            popup.ids.host.text = proxy.get('host') if proxy else ''
            popup.ids.port.text = proxy.get('port') if proxy else ''
            popup.ids.user.text = proxy.get('user') if proxy else ''
            popup.ids.password.text = proxy.get('password') if proxy else ''
            popup.on_dismiss = lambda: callback(popup)
            self._proxy_dialog = popup
        self._proxy_dialog.open()

    def plugin_dialog(self, name, label, dt):
        from .checkbox_dialog import CheckBoxDialog
        def callback(status):
            self.plugins.enable(name) if status else self.plugins.disable(name)
            label.status = 'ON' if status else 'OFF'
        status = bool(self.plugins.get(name))
        dd = self.plugins.descriptions.get(name)
        descr = dd.get('description')
        fullname = dd.get('fullname')
        d = CheckBoxDialog(fullname, descr, status, callback)
        d.open()

    def fee_status(self):
        return self.config.get_fee_status()

    def boolean_dialog(self, name, title, message, dt):
        from .checkbox_dialog import CheckBoxDialog
        CheckBoxDialog(title, message, getattr(self.app, name), lambda x: setattr(self.app, name, x)).open()

    def fx_status(self):
        fx = self.app.fx
        if fx.is_enabled():
            source = fx.exchange.name()
            ccy = fx.get_currency()
            return '%s [%s]' %(ccy, source)
        else:
            return _('None')

    def fx_dialog(self, label, dt):
        if self._fx_dialog is None:
            from .fx_dialog import FxDialog
            def cb():
                label.status = self.fx_status()
            self._fx_dialog = FxDialog(self.app, self.plugins, self.config, cb)
        self._fx_dialog.open()

    def reset_cw_pw(self):
        result = scan.reset_PIN(self.old_pin, self.new_pin)
        if result[0] == 1:
            self.app.show_error("reset successful")
        else:
            self.app.show_error("reset failed")
        self.rt.dismiss()

    def reset_pin(self, rt, old_pin, new_pin, comfirm_new_pin):
        if comfirm_new_pin != new_pin:
            self.app.show_error("Inconsistent password input")
        else:
            scan.nfc_init(self.reset_cw_pw)
            scan.nfc_enable()
            self.app.show_error("please touch your chard")
            self.rt = rt
            self.old_pin = old_pin
            self.new_pin = new_pin

    def unlock_cw_pw(self):
        import binascii
        result = scan.unlock_PIN(self.pin)
        resultHex = binascii.hexlify(bytes(result))
        if resultHex == b'9000':
            self.app.show_error("unlock successful")
        else:
            self.app.show_error("unlock failed")
        self.rt.dismiss()

    def unlock_pin(self, rt, password):
        scan.nfc_init(self.unlock_cw_pw)
        scan.nfc_enable()
        self.app.show_error("please touch your chard")
        self.pin = password
        self.rt = rt
'''