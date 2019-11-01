from kivy.app import App
from kivy.factory import Factory
from kivy.lang import Builder
from kivy.uix.label import Label
from electrum.gui.kivy.i18n import _
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.clock import Clock

Builder.load_string('''
         
<GetCosignerDialogInfo@ModalView>
    id: gci
    complete:""
    keystore_num:0
    m:0
    FloatLayout:
        orientation: 'vertical'
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 0.972, 0.976, 0.988, 1
            Rectangle:
                size: self.size
                pos: self.pos
        # HeadModel:
        #     message: _('Add Cosigner')
        #     action: root.dismiss
        ScrollView:
            orientation: 'vertical'
            pos_hint:{'x':.1, 'top':.9}
            size_hint: .8, 1
            GridLayout:
                row_force_default:True
                row_default_height: '55dp'
                orientation: 'vertical'
                height: self.minimum_height
                id: choices
                spacing:'10dp'
                cols: 1
                size_hint: 1, None
                # BoxLayout:
                #     orientation: 'vertical'
                #     height: '55dp'
                #     spacing: '15sp'
                #     canvas.before:
                #         Color:
                #             rgb: 1, .6, 1
                #         Rectangle:
                #             size: self.size
                #             pos: self.pos
                #     Label:
                #         text:'hahaha'
                #         color: (0,0,1,1)
                #         text_size: self.width, self.height
                #     Label:
                #         color: (0,0,0,1)
                #         text:"11111111"
                #         text_size: self.width, self.height
                # BoxLayout:
                #     orientation: 'vertical'
                #     height: '48dp'
                #     spacing: '15sp'
                #     canvas.before:
                #         Color:
                #             rgb: 1, .6, 1
                #         Rectangle:
                #             size: self.size
                #             pos: self.pos
                #     Label:
                #         text:'xixixixix'
                #         color: (0,0,1,1)
                #         text_size: self.width, self.height
                #     Label:
                #         text:"2222222222"
                #         color: (0,0,0,1)
                #         text_size: self.width, self.height
        
        Button:
            text:root.complete
            pos_hint: {'x':.05, 'y':.05}
            font_size: 20
            size_hint: .9, .1
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
        self.keystore = self.app.wizard.keystores
        self.keystore_num = len(self.keystore)
        self.m = int(self.app.wizard.m)
        print("xpub num = %s...." % len(self.keystore))
        self.complete = _("Complete") + '(' + str(len(self.keystore)) + "--" + str(self.app.wizard.n) + ')'
        layout = self.ids.choices
        n = 1

        #listtest = [1111, 22222, 333, 444, 555]
        #for xpub in listtest:
        for xpub in map(lambda x: x.xpub, self.keystore):
            box_layout = BoxLayout(orientation='vertical', spacing='10dp', height='55dp')
            from kivy.graphics import Color, Rectangle
            with box_layout.canvas:
                Color(1, .6, 1)
                r = Rectangle(pos=(box_layout.x, box_layout.y), size=(640, 55))
                print("box_layout.pos=%s size=%s" % (box_layout.pos, box_layout.size))

            def linksp(instance, *largs):
                r.pos = instance.x, instance.y
                r.size = instance.width, instance.height
                print("r.pos=(%s) r.size=%s" % (r.pos, r.size))

            box_layout.bind(pos=linksp, size=linksp)
            l_label = Label(text=("[color=ff3333]"+ _("cogigner") + str(n) + "[/color]"))
            #l_label.text_size = l_label.width, l_label.height
            #l_label.size = l_label.texture_size
            #l_label.text_size = l_label.setter('size')
            #l_label.halign = 'left'
            #l_label.text_size = (box_layout.width - dp(100), box_layout.height-dp(10))
            l_label2 = Label(text=_('[color=ff3333]%s...[/color]') % xpub[:65], markup=True)
            box_layout.add_widget(l_label)
            box_layout.add_widget(l_label2)
            n += 1
            layout.add_widget(box_layout)

        if self.keystore_num < self.app.wizard.n:
            btn1 = Button(text=_('+ Click to add'), size_hint=(1, .15))
            btn1.pos_hint_x = .3
            #btn1.pos_hint_y = box_layout.y-(n)*(55+10)
            btn1.color = [0.38, 0.509, 0.96, 1]
            btn1.background_normal = ''
            btn1.background_color = [1, 1, 1, 1]
            btn1.text_size = btn1.width, None
            #btn1.disabled = (self.keystore_num == self.app.wizard.n)
            def func():
                Clock.schedule_once(lambda x: app.popup_dialog('add_cosigner'))
            btn1.on_release = func
            layout.add_widget(btn1)

    def update_rect(self, instance, value):
        self.rect.pos = instance.pos
        self.rect.size = instance.size
