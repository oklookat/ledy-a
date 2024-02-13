package ru.oklookat.ledy

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi

private const val mServiceType = "_ledy._tcp."
private const val mServiceName = "ledy-server"

private val isApi34 =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

class ServerFinder(
    ctx: Context,
    onError: (err: Throwable) -> Unit,
    onFound: (String, Int) -> Unit
) {
    private var discoveringNow = false
    private val nsdManager = ctx.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DiscoveryListener(nsdManager, onError) { host, port ->
        this.stop()
        onFound(host, port)
    }

    fun find() {
        nsdManager.discoverServices(mServiceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        discoveringNow = true
    }

    fun stop() {
        if(!discoveringNow) return
        nsdManager.stopServiceDiscovery(discoveryListener)
        discoveringNow = false
    }
}

class DiscoveryListener(
    private val nsdManager: NsdManager,
    private val onError: (err: Throwable) -> Unit,
    onFound: (String, Int) -> Unit,

) : NsdManager.DiscoveryListener {


    private var serviceInfoCallbackRegistered = false
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private var serviceInfoCallback: ServiceInfoCallback = ServiceInfoCallback(onFound) {
        onError(it)
    }

    private val resolveListener = ResolveListener(onError, onFound)

    override fun onDiscoveryStarted(regType: String) {

    }

    override fun onServiceFound(service: NsdServiceInfo) {
        if (service.serviceType != mServiceType) {
            return
        }
        if (service.serviceName != mServiceName) {
            return
        }
        if (!isApi34) {
            nsdManager.resolveService(service, resolveListener)
        } else {
            nsdManager.registerServiceInfoCallback(
                service, {
                    it.run()
                },
                serviceInfoCallback
            )
            serviceInfoCallbackRegistered = true
        }

    }

    override fun onServiceLost(service: NsdServiceInfo) {

    }

    override fun onDiscoveryStopped(serviceType: String) {
        if (isApi34 && serviceInfoCallbackRegistered) {
            nsdManager.unregisterServiceInfoCallback(serviceInfoCallback)
            serviceInfoCallbackRegistered = false
        }
    }

    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        onError(Throwable("onStartDiscoveryFailed: code $errorCode"))
        nsdManager.stopServiceDiscovery(this)
    }

    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        onError(Throwable("onStopDiscoveryFailed: code $errorCode"))
        nsdManager.stopServiceDiscovery(this)
    }
}

class ResolveListener(
    private val onError: (err: Throwable) -> Unit,
    private val onFound: (String, Int) -> Unit,
) : NsdManager.ResolveListener {

    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        onError(Throwable("onResolveFailed: code $errorCode"))
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
        val hostPort = getHostPort(serviceInfo)
        onFound(hostPort.first, hostPort.second)
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class ServiceInfoCallback(
    private val onFound: (String, Int) -> Unit,
    private val onError: (err: Throwable) -> Unit
) :
    NsdManager.ServiceInfoCallback {

    override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
        onError(Throwable("serviceInfoCallbackRegistrationFailed: code $errorCode"))
    }

    override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
        val hostPort = getHostPort(serviceInfo)
        onFound(hostPort.first, hostPort.second)
    }

    override fun onServiceLost() {

    }

    override fun onServiceInfoCallbackUnregistered() {

    }

}

fun getHostPort(serviceInfo: NsdServiceInfo): Pair<String, Int> {
    val port = serviceInfo.port
    var host = ""
    if (isApi34) {
        if (serviceInfo.hostAddresses.isNotEmpty() && serviceInfo.hostAddresses[0].hostAddress != null) {
            host = serviceInfo.hostAddresses[0].hostAddress!!
        }
    } else {
        serviceInfo.host.toString()
    }
    return Pair(host, port)
}