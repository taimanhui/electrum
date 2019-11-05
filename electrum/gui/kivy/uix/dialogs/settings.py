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
            orientation: 'horizontal'
            pos_hint: {'x': 0, 'y':.78}
            size_hint: 1, None
            height: '50dp'
            padding: '25dp'
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
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y': .81}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x': 0, 'y':.69}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
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
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y':.73}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x': 0, 'y':.61}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
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
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(partial(root.language_dialog, self))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y':.64}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x': 0, 'y':.53}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("server setting")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(partial(root.language_dialog, self))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y':.56}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x': 0, 'y':.45}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp'
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("transaction setting")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(partial(root.language_dialog, self))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y':.48}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':0 , 'y':.3}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp' 
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
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y':.33}
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':0 , 'y':.22}
            size_hint: 1, None
            height: '50dp'
            padding: '30dp' 
            Label:
                text_size: self.width-dp(20), self.height-dp(13)
                size_hint: .7, None
                text: _("about(protocol)")
                color: [0.098, 0.098, 0.439, 1]
                halign: 'left'
            Button:
                size_hint: None, None
                size: 50, 30
                background_normal: ''
                background_color: [0.972, 0.976, 0.988, 1]
                on_release:
                    Clock.schedule_once(lambda x: app.popup_dialog('activation'))
                Image:
                    source: 'atlas://electrum/gui/kivy/theming/light/gotowallet'
                    y: self.parent.y+dp(5)
                    x: self.parent.x+dp(20)
                    size: 11, 18
                    allow_stretch: True
        CardSeparatorLine:
            pos_hint: {'x':.06, 'y':.25}

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