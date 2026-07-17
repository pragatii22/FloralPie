@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("com.cloudinary:cloudinary-http44:1.39.0")

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.cloudinary.Transformation

fun main() {
    // 1. Configure Cloudinary
    val config = mapOf(
        "cloud_name" to "tghpwp3g",
        "api_key" to "255732444319615",
        "api_secret" to "3yOXCZe5sMEuNWThu3b8KAOci8s",
        "secure" to true
    )
    val cloudinary = Cloudinary(config)

    println("--- Cloudinary Onboarding Test ---")

    // 2. Upload an image
    println("Uploading sample image from Cloudinary demo...")
    try {
        val uploadResult = cloudinary.uploader().upload(
            "https://res.cloudinary.com/demo/image/upload/sample.jpg",
            ObjectUtils.emptyMap()
        )

        val secureUrl = uploadResult["secure_url"] as String
        val publicId = uploadResult["public_id"] as String
        println("Success! Image uploaded.")
        println("Secure URL: $secureUrl")
        println("Public ID: $publicId")

        // 3. Get image details
        val width = uploadResult["width"]
        val height = uploadResult["height"]
        val format = uploadResult["format"]
        val bytes = uploadResult["bytes"]
        println("\n--- Image Details ---")
        println("Width: $width px")
        println("Height: $height px")
        println("Format: $format")
        println("File Size: $bytes bytes")

        // 4. Transform the image
        // f_auto: Automatic format selection (delivers the best format supported by the browser)
        // q_auto: Automatic quality selection (optimizes file size without noticeable quality loss)
        val transformedUrl = cloudinary.url()
            .transformation(Transformation<Transformation<*>>().fetchFormat("auto").quality("auto"))
            .generate(publicId)

        println("\nDone! Click link below to see optimized version of the image. Check the size and the format.")
        println(transformedUrl)
        
    } catch (e: Exception) {
        println("Error during Cloudinary operation: ${e.message}")
        e.printStackTrace()
    }
}

main()
