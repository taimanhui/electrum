import os
from kivy.app import App
from kivy.factory import Factory
from kivy.properties import ObjectProperty
from kivy.lang import Builder
from kivy.uix.label import Label
from electrum.util import base_units_list
from electrum.i18n import languages
from electrum.gui.kivy.i18n import _
from electrum.plugin import run_hook
from electrum import coinchooser

from .choice_dialog import ChoiceDialog

Builder.load_string('''
#:import partial functools.partial
#:import _ electrum.gui.kivy.i18n._
#:import os os

<TextInputModel@TextInput>
    text:_('Please input')
    size_hint: .5, 1
    multiline:False
    font_size: '15sp'
    foreground_color:(0.286, 0.286, 0.286, 1) if self.focus else [0.702, 0.702, 0.702, 1]
    background_normal: ''
    background_color: (1, 1, 1, 1)
    border:(10,10,10,10)
    halign: 'right'
    
<LabelModel@Label>
    text: ''
    font_size: '13sp'
    size_hint: .5, 1
    text_size: self.width-dp(60), self.height
    color:  [0.702, 0.702, 0.702, 1]
    valign: 'center'
        

<CreateMultiWalletDialog@ModalView>
    id: cmwallet
    path:''
    m:''
    n:''
    FloatLayout:
        id:cmw
        orientation: 'vertical'
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 0.972, 0.976, 0.988, 1
            Rectangle:
                size: self.size
                pos: self.pos        
        HeadModel:
            message: _('create wallet')
            action: partial(root.dismiss)        
        BoxLayout:
            orientation: 'vertical'
            pos_hint: {'x':.05, 'top':.89}
            size_hint: .9, .36
            canvas.before:
                Color:
                    rgb: 1, 1, 1
                Rectangle:
                    size: self.size
                    pos: self.pos
            Label:
                text: _('create multi wallet')
                bold:True
                font_size: '18sp'
                size_hint: .9, .25
                text_size: self.width-dp(60), self.height
                color:  [0.286, 0.286, 0.286, 1]
                valign: 'center'
            CardSeparatorLine:
                pos_hint: {'x':0, 'top':.68}
            BoxLayout:
                orientation: 'horizontal'
                size_hint: .95, .25
                LabelModel:
                    text: _('set wallet name')
                TextInputModel:
                    id:name
                    focus:True
            CardSeparatorSection:
                pos_hint: {'x':.05, 'top':.48}
            BoxLayout:
                orientation: 'horizontal'
                size_hint: .95, .25
                LabelModel:
                    text: _('Number of cosigner')
                TextInputModel:
                    id:n
            CardSeparatorSection:
                pos_hint: {'x':.05, 'top':.28}
            BoxLayout:
                orientation: 'horizontal'
                size_hint: .95, .25
                LabelModel:
                    text: _('Number of signatures')
                TextInputModel:
                    id:m
        Label:
            id:tips
            text:''
            pos_hint: {'x':.15, 'y':.2}
            size_hint: .7, .1
            color:  [.702, .702, .702, 1]
            font_size: '12sp'
            
        Button:
            text:_("Next")
            pos_hint: {'x':.15, 'y':.1}
            font_size: '16sp'
            size_hint: .7, None
            height: '50sp'
            background_normal: ''
            background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
            background_normal: '../../theming/light/card_btn.png'
            background_down: '../../theming/light/card_btn.png'
            disabled: name.text == '' or m.text == '' or n.text == ''
            on_release:
                cmwallet.dismiss()
                root.get_input_data(app)
                    
                
                
            
            
''')

class CreateMultiWalletDialog(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.path = ''
        Factory.ModalView.__init__(self)

        textinput_n = self.ids.n
        textinput_n.bind(focus=self.on_focus)
        textinput_m = self.ids.m
        textinput_m.bind(focus=self.on_focus)


    def on_focus(self, item, bt):
        status = False
        if self.ids.n.focus:
            tips = _("cosigner is all the manager and 2<=num<=15")
            status = True
        elif self.ids.m.focus:
            tips = _("Number of signatures, 2<=num<=15")
            status = True
        if status:
            self.ids.tips.text = "TIPS:%s" % tips
        else:
            self.ids.tips.text = ''

    def get_input_data(self, app):
        print("name = %s...." %self.ids.name.text)
        self.path = self.ids.name.text
        self.m = self.ids.m.text
        self.n = self.ids.n.text
        dirname = os.path.dirname(self.app.get_wallet_path())
        self.app.load_wallet_by_name(os.path.join(dirname, self.path), m=int(self.m), n=int(self.n))
