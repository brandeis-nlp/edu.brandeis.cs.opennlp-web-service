package edu.brandeis.cs.lappsgrid.util;

/**
 * @author Keith Suderman
 */
public class JsonUtil
{
   private JsonUtil()
   {
      // Don't allow instances of this object to be constructed.
   }

//   public static final Container getContainer(Data input) throws LappsException
//   {
//      long type = input.getDiscriminator();
//      if (type == Types.ERROR) {
//         // Data objects with an ERROR discriminator should not be
//         // passed in.
//         throw new LappsException(input.getPayload());
//      }
//      else if (type == Types.TEXT) {
//         Container container = new Container();
//         container.setText(input.getPayload());
//         return container;
//      }
//      else if (type == Types.JSON) {
//         return new Container(input.getPayload());
//      }
//      String typeName = DiscriminatorRegistry.get(type);
//      throw new LappsException("Unexpected Data object type: " + typeName);
//   }
}
