from kivy.app import App
from kivy.factory import Factory
from kivy.properties import ObjectProperty
from kivy.lang import Builder
from decimal import Decimal
from kivy.clock import Clock

from electrum.util import InvalidPassword
from electrum.gui.kivy.i18n import _

Builder.load_string('''

<PasswordDialog@ModalView>
    id: popup
    message: ''
    sub_message: ''
    endTimeStatus:False
    verifyResult:True
    FloatLayout:
        orientation: 'vertical'
        size_hint: 1, 1
        canvas.before:
            Color:
                rgba: 1, 1, 1, 1
            Rectangle:
                size: self.size
                pos: self.pos
        Label:
            id:ti
            text: root.message
            bold: True
            font_size: 25
            pos_hint: {'x':.07, 'y':.84}
            size_hint: .8, .1
            text_size: self.width - dp(40), self.height
            color:  [0, 0, 0, 1]
        Label:
            #text: 'text' if root.message==""
            text: '' if (root.message=="输入PIN码") else root.sub_message
            pos_hint: {'x':.07, 'y':.8}
            font_size: 13
            size_hint: .8, .1
            text_size: self.width - dp(40), self.height
            color:  [.745, .745, .745, 1]
        BoxLayout:
            orientation: 'horizontal'
            pos_hint: {'x':.1, 'y':.68}
            size_hint: .8, None
            height: '50dp'
            spacing:10
            TextInput:
                font_size: '20dp'
                #text：popup.update_neweast_pw
                text: "*" if len(kb.password)>1 else kb.password[-1] if (len(kb.password)==1 and root.endTimeStatus==False) else "*" if (len(kb.password)==1 and root.endTimeStatus==True) else ''
                #text: "*" if ((len(kb.password)>1) or (len(kb.password)==1 and root.endTimeStatus==False)) else kb.password[-1] if ((len(kb.password)==1 and root.endTimeStatus==True) else ''
                #size: self.texture_size
            TextInput:
                font_size: '20dp'
                text: "*" if len(kb.password)>2 else kb.password[-1] if (len(kb.password)==2 and root.endTimeStatus==False) else "*" if (len(kb.password)==2 and root.endTimeStatus==True) else ''
                #size: self.texture_size
            TextInput:
                font_size: '20dp'
                text: "*" if len(kb.password)>3 else kb.password[-1] if (len(kb.password)==3 and root.endTimeStatus==False) else "*" if (len(kb.password)==3 and root.endTimeStatus==True) else ''
                #size: self.texture_size
            TextInput:
                font_size: '20dp'
                text: "*" if len(kb.password)>4 else kb.password[-1] if (len(kb.password)==4 and root.endTimeStatus==False) else "*" if (len(kb.password)==4 and root.endTimeStatus==True) else ''
                #size: self.texture_size
            TextInput:
                font_size: '20dp'
                text: "*" if len(kb.password)>5 else kb.password[-1] if (len(kb.password)==5 and root.endTimeStatus==False) else "*" if (len(kb.password)==5 and root.endTimeStatus==True) else ''
                #size: self.texture_size
            TextInput:
                font_size: '20dp'
                text: "*" if len(kb.password)>6 else kb.password[-1] if (len(kb.password)==6 and root.endTimeStatus==False) else "*" if (len(kb.password)==6 and root.endTimeStatus==True) else ''
                #size: self.texture_size
        Label:
            text: "" if (root.verifyResult) else _('wrong pin,please input password again')
            pos_hint: {'x':.07, 'y':.6}
            font_size: 13
            size_hint: .8, .1
            text_size: self.width - dp(40), self.height
            color:  [.823, .411, .117, 1]
             
        GridLayout:
            id: kb
            size_hint: 1, None
            height: self.minimum_height
            update_amount: popup.update_password
            password: ''
            on_password: popup.on_password(self.password)
            spacing: '2dp'
            cols: 3
            KButton:
                text: '1'
            KButton:
                text: '2'
            KButton:
                text: '3'
            KButton:
                text: '4'
            KButton:
                text: '5'
            KButton:
                text: '6'
            KButton:
                text: '7'
            KButton:
                text: '8'
            KButton:
                text: '9'
            KButton:
                text: 'Clear'
            KButton:
                text: '0'
            KButton:
                text: '<'
''')

class PasswordDialog(Factory.ModalView):

    def init(self, app, wallet, message, on_success, on_failure, is_change=0):
        self.app = app
        self.verifyResult = True
        self.wallet = wallet
        self.lastpw = ''
        self.message = message

        print("message=%s....." %self.message)
        self.on_success = on_success
        self.on_failure = on_failure
        self.ids.kb.password = ''
        self.success = False
        self.is_change = is_change
        print("is_change = %s/////" % is_change)
        if self.is_change == 3:
            self.sub_message = _('Enter new PIN')
        elif self.is_change == 2:
            self.sub_message = _("Enter old PIN")
        print("sub message = %s======" %self.sub_message)
        self.pw = None
        self.new_password = None
        print("init in passwdialog.....")
        #self.title = 'Electrum' + ('  -  ' + self.wallet.basename() if self.wallet else '')

    def check_password(self, password):
        if self.is_change > 1:
            return True
        try:
            self.wallet.check_password(password)
            return True
        except InvalidPassword as e:
            return False

    def on_dismiss(self):
        if not self.success:
            if self.on_failure:
                self.on_failure()
            else:
                # keep dialog open
                return True
        else:
            if self.on_success:
                Clock.schedule_once(lambda dt: self.on_success(self.pw,), 0.1)

    def update_password(self, c):
        kb = self.ids.kb
        text = kb.password
        if c == '<':
            text = text[:-1]
        elif c == 'Clear':
            text = ''
        else:
            text += c
        kb.password = text
        self.endTimeStatus = False
        import threading
        timer = threading.Timer(1, self.get_end_time)
        timer.start()

    def get_end_time(self):
        self.endTimeStatus = True

    def on_password(self, pw):
        if len(pw) == 6:
            if self.check_password(pw):
                if self.is_change == 0:
                    self.success = True
                    self.pw = pw
                    self.message = _('Please wait...')
                    self.dismiss()
                #elif self.is_change == 1:
                    #self.pw = pw
                    #self.sub_message = _('Enter old PIN')
                    #self.ids.kb.password = ''
                    #self.is_change = 2
                elif self.is_change == 2:
                    self.pw = pw
                    self.sub_message = _('Enter new PIN')
                    self.ids.kb.password = ''
                    self.is_change = 3
                elif self.is_change == 3:
                    self.new_password = pw
                    self.sub_message = _('Confirm new PIN')
                    self.ids.kb.password = ''
                    self.is_change = 4
                elif self.is_change == 4:
                    self.success = pw == self.new_password
                    if pw == self.new_password:
                        self.verifyResult = True
                    else:
                        self.verifyResult = False
                        self.is_change=2

                    self.dismiss()
            else:
                self.app.show_error(_('Wrong PIN'))
                self.verifyResult = False
                self.ids.kb.password = ''

    #def on_password(self, pw):
        #if len(pw) == 6:
           # self.success = True
           # self.pw = pw
           # if self.is_change == 0:
            #    self.message = _('Please wait...')
           # elif self.is_change == 1:
           ##     self.message = _('Signing')
           # self.dismiss()
