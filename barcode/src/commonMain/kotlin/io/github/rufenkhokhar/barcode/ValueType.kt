package io.github.rufenkhokhar.barcode

/**
 * A sealed interface representing the various types of data that can be encoded within a barcode.
 * The specific type determines the structure of the data it holds.
 */
sealed interface ValueType {
    /**
     * Represents Wi-Fi access point information encoded in a barcode.
     * Scanning this allows for quick connection to a Wi-Fi network.
     *
     * @param ssid The network name (Service Set Identifier) of the Wi-Fi access point.
     * @param password The password for the Wi-Fi network.
     * @param type The encryption type of the Wi-Fi network.
     */
    data class Wifi(val ssid: String?, val password: String?, val type: WifiType) : ValueType
    /**
     * Represents a URL or web address.
     *
     * @param title The title of the webpage, if available.
     * @param url The URL itself.
     * @param desc A description of the URL content.
     * @param rawValue The original, un-parsed URL string from the barcode.
     */
    data class Url(val title: String?, val url: String?, val desc: String?,val rawValue: String?) : ValueType
    /**
     * Represents a calendar event.
     * Scanning this can add an event directly to a device's calendar.
     *
     * @param description A detailed description of the event.
     * @param start The start date and time of the event.
     * @param end The end date and time of the event.
     * @param location The location where the event will take place.
     * @param organizer The organizer of the event.
     * @param status The status of the event (e.g., confirmed, tentative).
     * @param summary A short summary or title for the event.
     */
    data class CalendarEvent(
        val description: String?,
        val start: CalendarEventDateTime?,
        val end: CalendarEventDateTime?,
        val location: String?,
        val organizer: String?,
        val status: String?,
        val summary: String?
    ) : ValueType

    /**
     * Represents contact information, often in a vCard format.
     *
     * @param name The name of the contact.
     * @param organization The organization or company the contact is affiliated with.
     * @param title The job title of the contact.
     * @param phones A list of phone numbers for the contact.
     * @param emails A list of email addresses for the contact.
     * @param urls A list of URLs associated with the contact.
     * @param addresses A list of postal addresses for the contact.
     */
    data class ContactInfo(
        val name: ContactInfoName?,
        val organization: String?,
        val title: String?,
        val phones: List<Phone>,
        val emails: List<Email>,
        val urls: List<String>,
        val addresses: List<PostalAddress>
    ) : ValueType
    /**
     * Represents data parsed from a driver's license barcode, typically a PDF417 code.
     * The fields correspond to the AAMVA standard.
     *
     * @param addressCity The city from the license holder's address.
     * @param addressState The state from the license holder's address.
     * @param addressStreet The street from the license holder's address.
     * @param addressZip The ZIP code from the license holder's address.
     * @param birthDate The birth date of the license holder.
     * @param documentType The type of document (e.g., driver's license, ID card).
     * @param expiryDate The expiration date of the license.
     * @param firstName The first name of the license holder.
     * @param middleName The middle name of the license holder.
     * @param lastName The last name of the license holder.
     * @param gender The gender of the license holder.
     * @param issueDate The date the license was issued.
     * @param issuingCountry The country that issued the license.
     * @param licenseNumber The unique identification number for the license.
     */
    data class DriverLicense(
        val addressCity: String?,
        val addressState: String?,
        val addressStreet: String?,
        val addressZip: String?,
        val birthDate: String?,
        val documentType: String?,
        val expiryDate: String?,
        val firstName: String?,
        val middleName: String?,
        val lastName: String?,
        val gender: String?,
        val issueDate: String?,
        val issuingCountry: String?,
        val licenseNumber: String?
    ) : ValueType
    /**
     * Represents an email.
     *
     * @param address The recipient's email address.
     * @param subject The subject line of the email.
     * @param body The main content of the email.
     * @param type The type of email (e.g., home, work).
     */
    data class Email(val address: String?, val subject: String?, val body: String?, val type: EmailType) : ValueType
    /**
     * Represents a geographic coordinate.
     *
     * @param lat The latitude of the location.
     * @param lng The longitude of the location.
     */
    data class Geo(val lat: Double, val lng: Double) : ValueType
    /**
     * Represents a phone number.
     *
     * @param number The phone number.
     * @param type The type of phone number (e.g., home, mobile).
     */
    data class Phone(val number: String?, val type: PhoneType) : ValueType
    /**
     * Represents an SMS (Short Message Service) message.
     *
     * @param phoneNumber The recipient's phone number.
     * @param message The content of the SMS message.
     */
    data class Sms(val phoneNumber: String?, val message: String?) : ValueType
    /**
     * Represents product information, often from a UPC or EAN barcode.
     *
     * @param raw The raw data string from the product barcode.
     */
    data class Product(val raw: String?) : ValueType
    /**
     * Represents plain text.
     *
     * @param text The text content encoded in the barcode.
     */
    data class Text(val text: String?) : ValueType
    /**
     * Represents an unknown or unsupported barcode data type.
     *
     * @param raw The raw, un-parsed data from the barcode.
     */
    data class Unknown(val raw: String?) : ValueType
}
/**
 * Represents the components of a person's name from contact information.
 *
 * @param formattedName The complete, formatted name.
 * @param first The first name.
 * @param middle The middle name.
 * @param last The last name.
 * @param prefix A name prefix (e.g., "Mr.", "Dr.").
 * @param suffix A name suffix (e.g., "Jr.", "Ph.D.").
 */
data class ContactInfoName(
    val formattedName: String?,
    val first: String?,
    val middle: String?,
    val last: String?,
    val prefix: String?,
    val suffix: String?
)

/**
 * Represents a postal address
 *
 * @param addressLines A list of strings representing the lines of the address.
 * @param type The type of address (e.g., home, work).
 */
data class PostalAddress(
    val addressLines: List<String>,
    val type: AddressType
)
/**
 * Represents a specific date and time for a calendar event.
 *
 * @param year The year of the event.
 * @param month The month of the event.
 * @param day The day of the month for the event.
 * @param hour The hour of the event.
 * @param minutes The minutes of the event.
 * @param seconds The seconds of the event.
 * @param rawValue The original, un-parsed date-time string from the barcode.
 */
data class CalendarEventDateTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minutes: Int,
    val seconds: Int,
    val rawValue: String?
)
/**
 * Enumerates the encryption types for a Wi-Fi network.
 */
enum class WifiType {
    /** An open, unsecured network. */
    TYPE_OPEN,
    /** Wi-Fi Protected Access. */
    TYPE_WPA,
    /** Wired Equivalent Privacy, an older and less secure protocol. */
    TYPE_WEP,
    /** The encryption type is unknown. */
    UNKNOWN
}
/**
 * Enumerates the types for an email address.
 */
enum class EmailType {
    /** A personal or home email address. */
    HOME,
    /** A work or business email address. */
    WORK,
    /** The email type is unknown. */
    UNKNOWN
}
/**
 * Enumerates the types for a phone number.
 */
enum class PhoneType {
    /** A home landline number. */
    HOME,
    /** A work number. */
    WORK,
    /** A fax number. */
    FAX,
    /** A mobile or cell phone number. */
    MOBILE,
    /** The phone type is unknown. */
    UNKNOWN
}
/**
 * Enumerates the types for a postal address.
 */
enum class AddressType {
    /** A home address. */
    HOME,
    /** A work or business address. */
    WORK,
    /** The address type is unknown. */
    UNKNOWN
}
