package com.waykichain.wallet.base.params

import com.waykichain.wallet.base.HashWriter
import com.waykichain.wallet.base.WaykiTxType
import com.waykichain.wallet.base.types.encodeInOldWay
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Utils
import org.bitcoinj.core.VarInt

/**
 * srcRegId: (regHeight-regIndex PubKeyHash)
 * destAddr: 20-byte PubKeyHash
 * fee Minimum 0.0001 wicc
 */
class WaykiDexCancelOrderTxParams(nValidHeight: Long, fees: Long,val userId: String, userPubKey: String?, feeSymbol: String,
                            val orderId: String) :
        BaseSignTxParams(feeSymbol, userPubKey, null, nValidHeight, fees, WaykiTxType.DEX_CANCEL_ORDER_TX, 1) {

    override fun getSignatureHash(): ByteArray {
        val ss = HashWriter()
        val pubKey = Utils.HEX.decode(userPubKey)
        val orderIdByte = Utils.HEX.decode(orderId).reversedArray()
        ss.add(VarInt(nVersion).encodeInOldWay())
                .add(nTxType.value)
                .add(VarInt(nValidHeight).encodeInOldWay())
                .writeUserId(userId, pubKey)
                .add(feeSymbol)
                .add(VarInt(fees).encodeInOldWay())
                .add(orderIdByte)
        val hash = Sha256Hash.hashTwice(ss.toByteArray())
        val hashStr = Utils.HEX.encode(hash)
        System.out.println("hash: $hashStr")

        return hash
    }

    override fun signTx(key: ECKey): ByteArray {
        val sigHash = this.getSignatureHash()
        val ecSig = key.sign(Sha256Hash.wrap(sigHash))
        signature = ecSig.encodeToDER()
        return signature!!
    }

    override fun serializeTx(): String {
        assert(signature != null)
        val sigSize = signature!!.size
        val pubKey = Utils.HEX.decode(userPubKey)
        val orderIdByte = Utils.HEX.decode(orderId).reversedArray()
        val ss = HashWriter()
        ss.add(VarInt(nTxType.value.toLong()).encodeInOldWay())
                .add(VarInt(nVersion).encodeInOldWay())
                .add(VarInt(nValidHeight).encodeInOldWay())
                .writeUserId(userId, pubKey)
                .add(feeSymbol)
                .add(VarInt(fees).encodeInOldWay())
                .add(orderIdByte)
                .writeCompactSize(sigSize.toLong())
                .add(signature)

        val hexStr = Utils.HEX.encode(ss.toByteArray())
        return hexStr
    }
}
