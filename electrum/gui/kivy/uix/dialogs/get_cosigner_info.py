from kivy.app import App
from kivy.factory import Factory
from kivy.lang import Builder
from kivy.uix.label import Label
from electrum.gui.kivy.i18n import _
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.clock import Clock
from kivy.uix.widget import Widget

Builder.load_string('''
         
<AddressLabel@Label>
    text_size: self.width, None
    halign: 'left'
    #valign: 'top'

<CosignerLayout@BoxLayout>
    cosignerinfo: ''
    xpubinfo: ''
    delete_xpub:None
    BoxLayout:
        spacing: '8dp'
        height: '55dp'
        orientation: 'vertical'
        canvas.before:
            Color:
                rgba: 0, 0, 0, 1
            Rectangle:
                size: self.size
                pos: self.pos
        Button:
            text:"c"
            text_size: self.width+10, self.height+10
            pos_hint:{'x':.95, 'top':.9}
            size_hint: .05, None
            height:'8dp'
            on_release:
                root.delete_xpub(root.xpubinfo)
        AddressLabel:
            text: root.cosignerinfo
            #shorten: True
            color:  [0, 0, 1, 1]
       # Widget
        AddressLabel:
            text: root.xpubinfo
            color: .699, .699, .699, 1
            font_size: '13sp'
            #shorten: True
        #Widget
<GetCosignerDialogInfo@ModalView>
    id: gci
    complete:""
    keystore_num:0
    m:0
    FloatLayout:
        id:top
        orientation: 'vertical'
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
        
        RecycleView:
            scroll_type: ['bars', 'content']
            bar_width: '15dp'
            viewclass: 'CosignerLayout'
            pos_hint: {'x':.05, 'top':.9}
            id: search_container
            spacing: '10dp'
            RecycleBoxLayout:
                orientation: 'vertical'
                default_size: None, dp(56)
                default_size_hint: .9, None
                size_hint_y: None
                height: self.minimum_height
                spacing: '10dp'
        
        Button:
            text:root.complete
            pos_hint: {'x':0, 'y':0}
            font_size: 20
            size_hint: 1, .1
            background_normal: ''
            background_color: [0.415, 0.462, 0.541, 1] if self.state == 'down' else [0.38, 0.509, 0.96, 1]
            background_normal: '../../theming/light/card_btn.png'
            background_down: '../../theming/light/card_btn.png'
            disabled_color: 0.38, 0.509, 0.96, .5
            disabled: root.keystore_num < root.m
            on_release:
                #gci.dismiss()
                Clock.schedule_once(lambda x: app.popup_dialog('create_wallet_success'))
            
''')

class GetCosignerDialogInfo(Factory.ModalView):

    def __init__(self, app):
        self.app = app
        self.active = False
        Factory.ModalView.__init__(self)
        self.update_complete()
        self.update()
        self.disable_add_button()

    def disable_add_button(self):
        container = self.ids.search_container
        if self.keystore_num < self.app.wizard.n:
            layout = self.ids.top
            btn1 = Button(text=_('+ Click to add'), size_hint=(.6, .15))
            btn1.pos = (container.width * 0.5, container.height * 0.5)
            btn1.color = [0.38, 0.509, 0.96, 1]
            btn1.background_normal = ''
            btn1.background_color = [1, 1, 1, 1]
            btn1.text_size = btn1.width, None
            # btn1.disabled = (self.keystore_num == self.app.wizard.n)
            def func():
                Clock.schedule_once(lambda x: self.app.popup_dialog('add_cosigner'))
            btn1.on_release = func
            layout.add_widget(btn1)

    def update_complete(self):
        self.keystore = self.app.wizard.keystores
        self.keystore_num = len(self.keystore)
        self.m = int(self.app.wizard.m)
        print("xpub num = %s...." % len(self.keystore))
        self.complete = _("Complete") + '(' + str(len(self.keystore)) + "--" + str(self.app.wizard.n) + ')'

    def update(self):
        container = self.ids.search_container
        self.num = 1
        cards = []
        for xpub in map(lambda x: x.xpub, self.keystore):
            cosigner = _("cogigner")+str(self.num)
            card = self.get_card(cosigner, xpub)
            cards.append(card)
            self.num += 1
        container.data = cards


    def get_card(self, cosigner, xpub):
        ci = {}
        ci['cosignerinfo'] = cosigner
        ci['xpubinfo'] = xpub
        ci['delete_xpub'] = self.delete_xpub
        print("ci = %s" % ci)
        return ci

    def delete_xpub(self, xpub):
        status = False
        if self.keystore_num == self.app.wizard.n:
            status = True
        self.app.wizard.delete_xpub(xpub)
        self.update_complete()
        self.update()
        if status:
            self.disable_add_button()

    def update_rect(self, instance, value):
        self.rect.pos = instance.pos
        self.rect.size = instance.size
