package src.model;

import java.time.LocalDateTime;

public class Prenotazione {
    private int id;
    private int idVisita;
    private String emailFruitore;
    private int numeroPersone;
    private LocalDateTime dataPrenotazione;
    private String codicePrenotazione;
    private String stato;

    public Prenotazione(String emailFruitore, int idVisita, int numeroPersone) {
        this.idVisita = idVisita;
        this.emailFruitore = emailFruitore;
        this.numeroPersone = numeroPersone;
        this.dataPrenotazione = LocalDateTime.now();
        this.codicePrenotazione = generaCodicePrenotazione();
        this.stato = "CONFERMATA";
    }

    private String generaCodicePrenotazione() {
        return "PRN" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getIdVisita() { return idVisita; }
    public void setIdVisita(int idVisita) { this.idVisita = idVisita; }

    public String getEmailFruitore() { return emailFruitore; }
    public void setEmailFruitore(String emailFruitore) { this.emailFruitore = emailFruitore; }

    public int getNumeroPersone() { return numeroPersone; }
    public void setNumeroPersone(int numeroPersone) { this.numeroPersone = numeroPersone; }
    
    public LocalDateTime getDataPrenotazione() { return dataPrenotazione; }
    public void setDataPrenotazione(LocalDateTime dataPrenotazione) { this.dataPrenotazione = dataPrenotazione; }
    
    public String getCodicePrenotazione() { return codicePrenotazione; }
    public void setCodicePrenotazione(String codicePrenotazione) { this.codicePrenotazione = codicePrenotazione; }
    
    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    @Override
    public String toString() {
        return "Prenotazione [codice=" + codicePrenotazione + ", persone=" + numeroPersone + ", stato=" + stato + "]";
    }
}
