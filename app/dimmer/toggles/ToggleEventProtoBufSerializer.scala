package dimmer.toggles

import akka.serialization.SerializerWithStringManifest

import com.trueaccord.scalapb.GeneratedMessage

/**
  * Martker trait for toggle events.
  */
trait ToggleEvent extends GeneratedMessage

/**
  * Serializer for toggle events.
  */
class ToggleEventProtoBufSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 4001

  final val CreatedManifest = classOf[ToggleCreated].getSimpleName

  override def manifest(o: AnyRef): String = o.getClass.getSimpleName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case CreatedManifest => ToggleCreated.parseFrom(bytes)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case e: ToggleEvent => e.toByteArray
  }
}
