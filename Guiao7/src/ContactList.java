import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ContactList extends ArrayList<Contact> {
    private List<Contact> contacts;

    public ContactList(){
        contacts = new ArrayList<>();

        contacts.add(new Contact("Gustavo",21,888888888,"Uminho", new ArrayList<>(List.of("gustavo@gmail.com"))));

    }
    // @TODO
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(contacts.size());
        for(Contact c : contacts){
            c.serialize(out);
        }
     }

    // @TODO
    public static ContactList deserialize(DataInputStream in) throws IOException {
        ContactList cl = new ContactList();
        int contactListSize = in.readInt();
        for(int i = 0; i<contactListSize; i++){
            cl.add(Contact.deserialize(in));
        }
        return cl;
    }

}
