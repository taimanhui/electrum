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
                rgba: 0.972, 0.976, 0.988, 1
            Rectangle:
                size: self.size
                pos: self.pos
        HeadModel:
            message: _('Settings')
            action: partial(root.dismiss)
        
    BoxLayout:
        orientation: 'vertical'
        padding: [10,100]
        spacing: 10
        Button:
            background_color: [0.97, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x : self.parent.x + dp(60)
                center_y: self.parent.center_y
                text: _("check/activation cold wallet")
                color: [0, 0, 0, 1]
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        Button:
            background_color: [0.972, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x: self.parent.x + dp(35)
                center_y: self.parent.center_y
                text: _("message manager")
                color: [0, 0, 0, 1]
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        Button:
            background_color: [0.972, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x : self.parent.x + dp(20)
                center_y: self.parent.center_y
                text: _("Language")
                color: [0, 0, 0, 1]
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        Button:
            background_color: [0.972, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x : self.parent.x + dp(35)
                center_y: self.parent.center_y
                text: _("server setting")
                color: [0, 0, 0, 1]
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        Button:
            background_color: [0.972, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x : self.parent.x + dp(25)
                center_y: self.parent.center_y
                text: _("transaction setting")
                color: [0, 0, 0, 1]
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        Button:
            background_color: [0.972, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x : self.parent.x + dp(30)
                center_y: self.parent.center_y
                text: _("service online")
                color: [0, 0, 0, 1]
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        Button:
            background_color: [0.972, 0.976, 0.988, 0] 
            on_release:
                Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                
            Image:
                source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                center_y: self.parent.center_y
                x: self.parent.width - dp(20)
                size: 11, 18
                allow_stretch: True
            Label:
                x : self.parent.x + dp(50)
                center_y: self.parent.center_y
                text: _("about(protocol)")
                color: [0, 0, 0, 1]
''')


class SettingsDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.plugins = self.app.plugins
        self.config = self.app.electrum_config
        Factory.ModalView.__init__(self)
        # self.font_cn = self.app.font_cn
        # print("self.app.font_cn = %s..." % self.font_cn)
        # layout = self.ids.setviewlayout
        # layout.bind(minimum_height=layout.setter('height'))
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
                print("key %s++++" % key)
                self.config.set_key("language", key, True)
                item.lang = self.get_language_name()
                self.app.language = key

            self._language_dialog = ChoiceDialog(_('Language'), languages, l, cb)
        print("language test....")
        self._language_dialog.open()

    def get_language_name(self):
        return languages.get(self.config.get('language', 'en_UK'), '')