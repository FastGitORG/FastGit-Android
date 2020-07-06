package com.fastaccess.provider.rest.resolvers

import android.util.Log
import com.fastaccess.helper.PrefGetter
import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException

class FastGitDns : Dns {
    private val TAG = "FastGitDns"

    fun needProxy(hostname: String): Boolean {
        if (hostname.equals("raw.githubusercontent.com")
                || hostname.equals("github.com")
                || hostname.equals("github.githubassets.com")
                || hostname.equals("customer-stories-feed.github.com")
                || hostname.equals("codeload.github.com")
        ) {
            return true
        }
        return false
    }

    @Throws(UnknownHostException::class)
    override fun lookup(hostname: String): List<InetAddress> {
        if (needProxy(hostname)) {
            val mode = PrefGetter.getFastGitProxyMode()
            val sniproxyDomain = PrefGetter.getFastgitSniproxyDnsAddress()
            val sniproxyIpPool = PrefGetter.getFastgitSniproxyIpPool()
            try {
                if (!mode.isNullOrBlank() && mode == "1" && !sniproxyDomain.isNullOrBlank()) {
                    return InetAddress.getAllByName(sniproxyDomain).toList()
                }
                if (!mode.isNullOrBlank() && mode == "2" && !sniproxyIpPool.isNullOrBlank()) {
                    val ips = sniproxyIpPool.split("\n").map { it.trim() };
                    return ips.map { InetAddress.getAllByName(it) }.flatMap { it.asIterable() }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "failed get address, mode: ${mode}, sniproxyDomain: ${sniproxyDomain}, sniproxyIpPool: ${sniproxyIpPool}", e)
            }
        }
        return Dns.SYSTEM.lookup(hostname)
    }
}