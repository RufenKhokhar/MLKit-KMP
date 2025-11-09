package io.github.rufenkhokhar.barcode

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import io.github.rufenkhokhar.corevision.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalGetImage::class)
internal class AndroidBarcodeProcessor(scannerOptions: BarcodeScannerOptions) : BarcodeProcessor {
    private val barcodeScanner = BarcodeScanning.getClient(scannerOptions)


    override suspend fun processImageFrame(image: PlatformImage): Result<List<ScanResult>> {
        return withContext(Dispatchers.Default) {
            when (image) {
                is PlatformBitmapImage -> {
                    processImage(image.nativeImage)
                }

                is PlatformImageProxy -> {
                    val imageProxy = image.nativeImage
                    val inputImage = InputImage.fromMediaImage(
                        imageProxy.image!!,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    processImage(inputImage)
                }

                is PlatformStreamImage -> {
                    processImage(image.nativeImage)
                }
            }
        }
    }


    private suspend fun processImage(inputImage: InputImage): Result<List<ScanResult>> {
        return suspendCoroutine { emitter ->
            barcodeScanner.process(inputImage)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        val resultData = result.result.map { barcode ->
                            parseBarcode(barcode)
                        }
                        emitter.resume(Result.success(resultData))
                    } else {
                        emitter.resume(Result.failure(Throwable(result.exception)))
                    }
                }
        }
    }

    private fun parseBarcode(barcode: Barcode): ScanResult {
        val resValueType = when (barcode.valueType) {
            Barcode.TYPE_URL -> {
                val url = barcode.url
                ValueType.Url(
                    title = url?.title,
                    url = url?.url,
                    desc = null,
                    rawValue = barcode.rawValue
                )
            }

            Barcode.TYPE_WIFI -> {
                val wifi = barcode.wifi
                val wifiType = when (wifi?.encryptionType) {
                    Barcode.WiFi.TYPE_OPEN -> WifiType.TYPE_OPEN
                    Barcode.WiFi.TYPE_WPA -> WifiType.TYPE_WPA
                    Barcode.WiFi.TYPE_WEP -> WifiType.TYPE_WEP
                    else -> WifiType.UNKNOWN
                }
                ValueType.Wifi(
                    ssid = wifi?.ssid,
                    password = wifi?.password,
                    type = wifiType
                )
            }

            Barcode.TYPE_CALENDAR_EVENT -> {
                val ce = barcode.calendarEvent
                val start = ce?.start
                val end = ce?.end
                val reStart = CalendarEventDateTime(
                    year = start?.year ?: -1,
                    month = start?.month ?: -1,
                    day = start?.day ?: -1,
                    hour = start?.hours ?: -1,
                    minutes = start?.minutes ?: -1,
                    seconds = start?.seconds ?: -1,
                    rawValue = start?.rawValue
                )
                val reEnd = CalendarEventDateTime(
                    year = end?.year ?: -1,
                    month = end?.month ?: -1,
                    day = end?.day ?: -1,
                    hour = end?.hours ?: -1,
                    minutes = end?.minutes ?: -1,
                    seconds = end?.seconds ?: -1,
                    rawValue = end?.rawValue
                )
                ValueType.CalendarEvent(
                    description = ce?.description,
                    start = reStart,
                    end = reEnd,
                    location = ce?.location,
                    organizer = ce?.organizer,
                    status = ce?.status,
                    summary = ce?.summary
                )
            }

            Barcode.TYPE_CONTACT_INFO -> {
                val c = barcode.contactInfo
                val name = c?.name
                ValueType.ContactInfo(
                    name = ContactInfoName(
                        formattedName = name?.formattedName,
                        first = name?.first,
                        middle = name?.middle,
                        last = name?.last,
                        prefix = name?.prefix,
                        suffix = name?.suffix
                    ),
                    organization = c?.organization,
                    title = c?.title,
                    phones = c?.phones?.map {
                        ValueType.Phone(
                            number = it.number,
                            type = when (it.type) {
                                Barcode.Phone.TYPE_HOME -> PhoneType.HOME
                                Barcode.Phone.TYPE_WORK -> PhoneType.WORK
                                Barcode.Phone.TYPE_FAX -> PhoneType.FAX
                                Barcode.Phone.TYPE_MOBILE -> PhoneType.MOBILE
                                else -> PhoneType.UNKNOWN
                            }
                        )
                    }.orEmpty(),
                    emails = c?.emails?.map {
                        ValueType.Email(
                            address = it.address,
                            subject = it.subject,
                            body = it.body,
                            type = when (it.type) {
                                Barcode.Email.TYPE_HOME -> EmailType.HOME
                                Barcode.Email.TYPE_WORK -> EmailType.WORK
                                else -> EmailType.UNKNOWN
                            }
                        )
                    }.orEmpty(),
                    urls = c?.urls?.toList().orEmpty(),
                    addresses = c?.addresses?.map {
                        PostalAddress(
                            addressLines = it.addressLines.toList(),
                            type = when (it.type) {
                                Barcode.Address.TYPE_HOME -> AddressType.HOME
                                Barcode.Address.TYPE_WORK -> AddressType.WORK
                                else -> AddressType.UNKNOWN
                            }
                        )
                    }.orEmpty()
                )
            }

            Barcode.TYPE_EMAIL -> {
                val e = barcode.email
                ValueType.Email(
                    address = e?.address,
                    subject = e?.subject,
                    body = e?.body,
                    type = when (e?.type) {
                        Barcode.Email.TYPE_HOME -> EmailType.HOME
                        Barcode.Email.TYPE_WORK -> EmailType.WORK
                        else -> EmailType.UNKNOWN
                    }
                )
            }

            Barcode.TYPE_PHONE -> {
                val p = barcode.phone
                ValueType.Phone(
                    number = p?.number,
                    type = when (p?.type) {
                        Barcode.Phone.TYPE_HOME -> PhoneType.HOME
                        Barcode.Phone.TYPE_WORK -> PhoneType.WORK
                        Barcode.Phone.TYPE_FAX -> PhoneType.FAX
                        Barcode.Phone.TYPE_MOBILE -> PhoneType.MOBILE
                        else -> PhoneType.UNKNOWN
                    }
                )
            }

            Barcode.TYPE_SMS -> {
                val s = barcode.sms
                ValueType.Sms(
                    phoneNumber = s?.phoneNumber,
                    message = s?.message
                )
            }

            Barcode.TYPE_GEO -> {
                val g = barcode.geoPoint
                ValueType.Geo(
                    lat = g?.lat ?: 0.0,
                    lng = g?.lng ?: 0.0
                )
            }

            Barcode.TYPE_DRIVER_LICENSE -> {
                val d = barcode.driverLicense
                ValueType.DriverLicense(
                    addressCity = d?.addressCity,
                    addressState = d?.addressState,
                    addressStreet = d?.addressStreet,
                    addressZip = d?.addressZip,
                    birthDate = d?.birthDate,
                    documentType = d?.documentType,
                    expiryDate = d?.expiryDate,
                    firstName = d?.firstName,
                    middleName = d?.middleName,
                    lastName = d?.lastName,
                    gender = d?.gender,
                    issueDate = d?.issueDate,
                    issuingCountry = d?.issuingCountry,
                    licenseNumber = d?.licenseNumber
                )
            }

            Barcode.TYPE_PRODUCT -> {
                ValueType.Product(raw = barcode.rawValue)
            }

            Barcode.TYPE_TEXT -> {
                ValueType.Text(text = barcode.displayValue ?: barcode.rawValue)
            }

            else -> ValueType.Unknown(raw = barcode.rawValue)
        }
        val boundingBox = BoundingBox(
            left = barcode.boundingBox?.left?.toFloat() ?: 0f,
            right = barcode.boundingBox?.right?.toFloat() ?: 0f,
            top = barcode.boundingBox?.top?.toFloat() ?: 0f,
            bottom = barcode.boundingBox?.bottom?.toFloat() ?: 0f
        )
        return ScanResult(
            boundingBox = boundingBox,
            rawValue = barcode.rawValue,
            displayValue = barcode.displayValue,
            valueType = resValueType,
            cornerPoints = barcode.cornerPoints?.map { Point(it.x,it.y) }.orEmpty(),
        )
    }
}