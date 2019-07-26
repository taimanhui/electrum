# -*- coding: utf-8 -*-
import json
from flask import Flask, request, render_template
from flask_restful import Resource, Api, reqparse
from flask_sqlalchemy import SQLAlchemy
from pycoin.tx.Tx import Tx
from pycoin.tx.TxOut import TxOut
from pycoin.tx.script.flags import SIGHASH_ALL
import binascii
import config
from bitcoinrpc.authproxy import AuthServiceProxy

bitcoind = AuthServiceProxy(config.RPC_URL)
app = Flask(__name__)
api = Api(app)
app.config['SQLALCHEMY_DATABASE_URI'] = config.SQLALCHEMY_DATABASE_URI

parser = reqparse.RequestParser()
parser.add_argument('data')

db = SQLAlchemy(app)


class Txs(db.Model):
    id = db.Column('id', db.Integer, primary_key=True)
    tx_hex = db.Column(db.String())
    address = db.Column(db.String())
    json = db.Column(db.String())
    confirmation = db.Column(db.Integer())
    Signed = db.Column(db.Integer())


db.create_all()


class AddTx(Resource):
    def post(self):
        req = json.loads(request.json)
        tx = req['tx']
        tx_hex = req['hex']
        addresses = []
        for txin in tx['inputs']:
            addr = txin['address']
            if addr not in addresses:
                addresses.append(addr)

        # not support different addr now
        assert (len(addresses) == 1)

        address = addresses[0]
        try:
            txdb = Txs()
            txdb.address = address
            txdb.tx_hex = tx_hex
            txdb.json = request.json
            db.session.add(txdb)
            db.session.commit()
        except Exception as e:
            return "can't add tx: {}".format(e)
        return {}


class SignTx(Resource):
    def get(self, address):
        tx = Txs.query.filter_by(address=address).first()
        res = {}
        res['hex'] = tx.tx_hex
        res['rid'] = tx.id
        return res

    def post(self, address):
        req = json.loads(request.json)
        rid = req['rid']
        tx_hex = req['tx_hex']
        tx = Txs.query.filter_by(id=rid).first()
        tx.tx_hex = tx_hex
        db.session.commit()
        return tx_hex


def get_sig_hash(tx_hex):
    tx = Tx.from_hex(tx_hex)
    checker = tx.SolutionChecker(tx)
    for txin in tx.txs_in:
        pre_tx = bitcoind.getrawtransaction("%s" % txin.previous_hash, 1)
        vout = pre_tx['vout'][txin.previous_index]
        amount = vout['value']
        scriptPubKey = vout['scriptPubKey']['hex']
        tx_out = TxOut(amount, binascii.unhexlify(scriptPubKey))
        tx.unspents.append(tx_out)

    sig_hashs = []
    for idx, txin in enumerate(tx.txs_in):
        sig_hash = checker.signature_hash(txin.script, idx, SIGHASH_ALL)
        sig_hashs.append(sig_hash)
    return sig_hash


api.add_resource(AddTx, '/api/add')
api.add_resource(SignTx, '/api/sign/<string:address>')


@app.route('/home')
def home():
    return render_template('home.html')


if __name__ == '__main__':
    app.run(host="0.0.0.0", debug=config.DEBUG)
