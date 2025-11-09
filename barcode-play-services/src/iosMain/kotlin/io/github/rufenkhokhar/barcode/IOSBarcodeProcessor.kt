package io.github.rufenkhokhar.barcode

import cocoapods.GoogleMLKit.MLKBarcode
import cocoapods.GoogleMLKit.MLKBarcodeAddress
import cocoapods.GoogleMLKit.MLKBarcodeAddressTypeHome
import cocoapods.GoogleMLKit.MLKBarcodeAddressTypeWork
import cocoapods.GoogleMLKit.MLKBarcodeEmail
import cocoapods.GoogleMLKit.MLKBarcodeEmailTypeHome
import cocoapods.GoogleMLKit.MLKBarcodeEmailTypeWork
import cocoapods.GoogleMLKit.MLKBarcodePhone
import cocoapods.GoogleMLKit.MLKBarcodePhoneTypeFax
import cocoapods.GoogleMLKit.MLKBarcodePhoneTypeHome
import cocoapods.GoogleMLKit.MLKBarcodePhoneTypeMobile
import cocoapods.GoogleMLKit.MLKBarcodePhoneTypeWork
import cocoapods.GoogleMLKit.MLKBarcodeScanner
import cocoapods.GoogleMLKit.MLKBarcodeScannerOptions
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeCalendarEvent
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeContactInfo
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeDriversLicense
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeEmail
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeGeographicCoordinates
import cocoapods.GoogleMLKit.MLKBarcodeValueTypePhone
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeProduct
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeSMS
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeText
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeURL
import cocoapods.GoogleMLKit.MLKBarcodeValueTypeWiFi
import cocoapods.GoogleMLKit.MLKBarcodeWiFiEncryptionTypeOpen
import cocoapods.GoogleMLKit.MLKBarcodeWiFiEncryptionTypeWEP
import cocoapods.GoogleMLKit.MLKBarcodeWiFiEncryptionTypeWPA
import cocoapods.GoogleMLKit.MLKVisionImage
import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import objcnames.protocols.MLKCompatibleImageProtocol
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSNotFound
import platform.Foundation.NSValue
import platform.UIKit.CGPointValue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Suppress("UNCHECKED_CAST_TO_FORWARD_DECLARATION")
@OptIn(ExperimentalForeignApi::class)
internal class IOSBarcodeProcessor(
    barcodeScannerOptions: MLKBarcodeScannerOptions
) : BarcodeProcessor {

    private val barcodeScanner = MLKBarcodeScanner.barcodeScannerWithOptions(barcodeScannerOptions)


    override suspend fun processImageFrame(image: PlatformImage): Result<List<ScanResult>> {
        return withContext(Dispatchers.Default) {
            when (image) {
                is PlatformBitmapImage -> {
                    processImage(image.nativeImage)
                }

                is PlatformImageProxy -> {
                    processImage(image.nativeImage)
                }

                is PlatformStreamImage -> {
                    val streamImage = image.nativeImage
                    val inputImage = MLKVisionImage(streamImage).apply {
                        setOrientation(image.orientation)
                    }
                    processImage(inputImage)
                }
            }
        }
    }


    private suspend fun processImage(inputImage: MLKVisionImage): Result<List<ScanResult>> {
        return suspendCoroutine { emitter ->
            barcodeScanner.processImage(inputImage as MLKCompatibleImageProtocol) { barcodes, error ->
                if (barcodes != null) {
                    val result = barcodes.map { barcode ->
                        parseBarcode(barcode as MLKBarcode)
                    }
                    emitter.resume(Result.success(result))
                } else {
                    emitter.resume(Result.failure(Throwable(error?.debugDescription)))
                }
            }
        }
    }

    private fun parseBarcode(barcode: MLKBarcode): ScanResult {
        val boundingBox = barcode.frame.useContents {
            BoundingBox(
                left = origin.x.toFloat(),
                right = origin.x.toFloat() + size.width.toFloat(),
                top = origin.y.toFloat(),
                bottom = origin.y.toFloat() + size.height.toFloat()
            )
        }
        val rawValue = barcode.rawValue
        val displayValue = barcode.displayValue
        val cornerPoints = barcode.cornerPoints?.mapNotNull { value ->
            (value as? NSValue)?.CGPointValue()?.useContents {
                    io.github.rufenkhokhar.corevision.Point(
                        x = x.toInt(),
                        y = y.toInt()
                    )
                }
        }

        val valueType = when (barcode.valueType) {
            MLKBarcodeValueTypeURL -> {
                val bookmarkUrl = barcode.URL
                ValueType.Url(
                    title = bookmarkUrl?.title,
                    url = bookmarkUrl?.url,
                    rawValue = rawValue,
                    desc = bookmarkUrl?.description
                )
            }

            MLKBarcodeValueTypeWiFi -> {
                val wiFi = barcode.wifi
                ValueType.Wifi(
                    ssid = wiFi?.ssid,
                    password = wiFi?.password,
                    type = mapWifi(wiFi?.type)
                )
            }

            MLKBarcodeValueTypeCalendarEvent -> {
                val event = barcode.calendarEvent
                ValueType.CalendarEvent(
                    description = event?.eventDescription,
                    start = event?.start.toModel(),
                    end = event?.end.toModel(),
                    location = event?.location,
                    organizer = event?.organizer,
                    status = event?.status,
                    summary = event?.summary
                )
            }

            MLKBarcodeValueTypeContactInfo -> {
                val contactInfo = barcode.contactInfo
                val personName = contactInfo?.name
                ValueType.ContactInfo(
                    name = ContactInfoName(
                        formattedName = personName?.formattedName,
                        first = personName?.first,
                        middle = personName?.middle,
                        last = personName?.last,
                        prefix = personName?.prefix,
                        suffix = personName?.suffix
                    ),
                    organization = contactInfo?.organization,
                    title = contactInfo?.jobTitle,
                    phones = contactInfo?.phones?.map { phone ->
                        phone as MLKBarcodePhone
                        ValueType.Phone(
                            number = phone.number,
                            type = mapPhoneType(phone.type)
                        )
                    }?.toList().orEmpty(),
                    emails = contactInfo?.emails?.map { email ->
                        email as MLKBarcodeEmail
                        ValueType.Email(
                            address = email.address,
                            subject = email.subject,
                            body = email.body,
                            type = mapEmailType(email.type)
                        )
                    }?.toList().orEmpty(),
                    urls = contactInfo?.urls?.map { it as String }.orEmpty(),
                    addresses = contactInfo?.addresses?.map { address ->
                        address as MLKBarcodeAddress
                        PostalAddress(
                            addressLines = address.addressLines?.map { it as String }.orEmpty(),
                            type = mapAddressType(address.type)
                        )
                    }?.toList().orEmpty()
                )
            }

            MLKBarcodeValueTypeEmail -> {
                val email = barcode.email
                ValueType.Email(
                    address = email?.address,
                    subject = email?.subject,
                    body = email?.body,
                    type = mapEmailType(email?.type)
                )
            }

            MLKBarcodeValueTypePhone -> {
                val phone = barcode.phone
                ValueType.Phone(
                    number = phone?.number,
                    type = mapPhoneType(phone?.type)
                )
            }

            MLKBarcodeValueTypeSMS -> {
                val sms = barcode.sms
                ValueType.Sms(
                    phoneNumber = sms?.phoneNumber,
                    message = sms?.message
                )
            }

            MLKBarcodeValueTypeGeographicCoordinates -> {
                val geoPoint = barcode.geoPoint
                ValueType.Geo(
                    lat = geoPoint?.latitude ?: 0.0,
                    lng = geoPoint?.longitude ?: 0.0
                )
            }

            MLKBarcodeValueTypeDriversLicense -> {
                val driverLicense = barcode.driverLicense
                ValueType.DriverLicense(
                    addressCity = driverLicense?.addressCity,
                    addressState = driverLicense?.addressState,
                    addressStreet = driverLicense?.addressStreet,
                    addressZip = driverLicense?.addressZip,
                    birthDate = driverLicense?.birthDate,
                    documentType = driverLicense?.documentType,
                    expiryDate = driverLicense?.expiryDate,
                    firstName = driverLicense?.firstName,
                    middleName = driverLicense?.middleName,
                    lastName = driverLicense?.lastName,
                    gender = driverLicense?.gender,
                    issueDate = driverLicense?.issuingDate,
                    issuingCountry = driverLicense?.issuingCountry,
                    licenseNumber = driverLicense?.licenseNumber
                )
            }

            MLKBarcodeValueTypeProduct -> {
                ValueType.Product(raw = rawValue)
            }

            MLKBarcodeValueTypeText -> {
                ValueType.Text(text = displayValue ?: rawValue)
            }

            else -> ValueType.Unknown(raw = rawValue)
        }


        return ScanResult(
            boundingBox = boundingBox,
            rawValue = rawValue,
            displayValue = displayValue,
            valueType = valueType,
            cornerPoints = cornerPoints.orEmpty(),
        )
    }
    fun Long?.safe(): Int = this?.let { (if (it == NSNotFound) -1 else it).toInt() } ?: -1

    private fun mapWifi(type: Long?): WifiType = when (type) {
        MLKBarcodeWiFiEncryptionTypeOpen -> WifiType.TYPE_OPEN
        MLKBarcodeWiFiEncryptionTypeWPA -> WifiType.TYPE_WPA
        MLKBarcodeWiFiEncryptionTypeWEP -> WifiType.TYPE_WEP
        else -> WifiType.UNKNOWN
    }

    private fun mapEmailType(type: Long?): EmailType = when (type) {
        MLKBarcodeEmailTypeHome -> EmailType.HOME
        MLKBarcodeEmailTypeWork -> EmailType.WORK
        else -> EmailType.UNKNOWN
    }

    private fun mapPhoneType(type: Long?): PhoneType = when (type) {
        MLKBarcodePhoneTypeHome -> PhoneType.HOME
        MLKBarcodePhoneTypeWork -> PhoneType.WORK
        MLKBarcodePhoneTypeFax -> PhoneType.FAX
        MLKBarcodePhoneTypeMobile -> PhoneType.MOBILE
        else -> PhoneType.UNKNOWN
    }

    private fun mapAddressType(type: Long?): AddressType = when (type) {
        MLKBarcodeAddressTypeHome -> AddressType.HOME
        MLKBarcodeAddressTypeWork -> AddressType.WORK
        else -> AddressType.UNKNOWN
    }

    private fun NSDate?.toModel(): CalendarEventDateTime {
        if (this == null) {
            return CalendarEventDateTime(
                year = -1, month = -1, day = -1,
                hour = -1, minutes = -1, seconds = -1,
                rawValue = null
            )
        }
        val calendar = NSCalendar.currentCalendar
        val units = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
                NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond
        val comps = calendar.components(units, fromDate = this)

        val iso = NSISO8601DateFormatter().stringFromDate(this)
        return CalendarEventDateTime(
            year = comps.year.safe(),
            month = comps.month.safe(),
            day = comps.day.safe(),
            hour = comps.hour.safe(),
            minutes = comps.minute.safe(),
            seconds = comps.second.safe(),
            rawValue = iso
        )
    }
}




