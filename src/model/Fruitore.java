package src.model;

public class Fruitore extends Utente {

    public Fruitore(String nome, String cognome, String email, String password) {
        super(email, password, nome, cognome);
    }

    @Override
    public String toString() {
        return "Fruitore{" +
                "nome='" + getNome() + '\'' +
                ", cognome='" + getCognome() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }

    

}
