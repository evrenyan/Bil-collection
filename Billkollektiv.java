import java.awt.event.*;
import javax.swing.*;
import java.util.Scanner;

abstract class Bil {
    final String bilnummer;
    final int pris;
    Bil neste = null;
    Bil forrige = null;
    public Bil(String bilnummer, int pris) {
        this.bilnummer = bilnummer;
        this.pris = pris;
    }
    public boolean erElbil() {
        return false;
    }
    @Override
    public String toString() {
        return bilnummer + " pris " + pris;
    }
    public Bil finnBilR(Dialog dialog, boolean kunElektrisk) {
        if (! kunElektrisk && dialog.svarJaEllerNei("Liker du " + this + "?")) {
            return this;
        }
        if (neste != null) {
            return neste.finnBilR(dialog, kunElektrisk);
        }
        return null;
    }
}
class PersonBil extends Bil {
    final int antallPassasjer;
    public PersonBil(String bilnummer, int pris, int antallPassasjer) {
        super(bilnummer, pris);
        this.antallPassasjer = antallPassasjer;
    }
    @Override
    public String toString() {
        return "personbil " + super.toString() + " " + antallPassasjer + " pas";
    }
}
class Varebil extends Bil {
    final int lastevolum;
    public Varebil(String bilnummer, int pris, int lastevolum) {
        super(bilnummer, pris);
        this.lastevolum = lastevolum;
    }
    @Override
    public String toString() {
        return "Varebil " + super.toString() + " " + lastevolum + " volum";
    }
}
interface Elektrisk {
    int hentBatteriKapasitet();
}
class ElektriskPersonBil extends PersonBil implements Elektrisk  {
    final int batteriKapasitet;
    public ElektriskPersonBil(String bilnummer, int pris,int antallPassasjer,  int batteriKapasitet) {
        super(bilnummer, pris, antallPassasjer);
        this.batteriKapasitet = batteriKapasitet;
    }
    @Override
    public boolean erElbil() {
        return true;
    }
    @Override
    public String toString () {
        return super.toString() + " " + batteriKapasitet + " Kwh";
    }
    @Override
    public int hentBatteriKapasitet() {
        return batteriKapasitet;
    }
    @Override
    public Bil finnBilR (Dialog dialog, boolean kunElektrisk) {
        if (dialog.svarJaEllerNei("liker du " + this + "?")) {
            return this;
        }
        if (neste != null) {
            return neste.finnBilR(dialog, kunElektrisk);
        }
        return null;
    }
}
class ElektriskVarebil extends Varebil implements Elektrisk {
    final int batterikapasitet;
    public ElektriskVarebil(String bilnummer, int pris, int lastevolum, int batterikapasitet) {
        super(bilnummer, pris, lastevolum);
        this.batterikapasitet = batterikapasitet;
    }
    @Override
    public boolean erElbil() {
        return true;
    }
    @Override
    public int hentBatteriKapasitet() {
        return batterikapasitet;
    }
    @Override
    public String toString () {
        return super.toString() + " " + batterikapasitet + " Kwh";
    }
    @Override
    public Bil finnBilR (Dialog dialog, boolean kunElektrisk) {
        if (dialog.svarJaEllerNei("liker du " + this + "?")) {
            return this;
        }
        if (neste != null) {
            return neste.finnBilR(dialog, kunElektrisk);
        }
        return null;
    }
}
interface Dialog {
    boolean svarJaEllerNei(String sporsmaal);
}
class TastaturDialog implements Dialog {
    Scanner tastatur = new Scanner(System.in);
    @Override
    public boolean svarJaEllerNei(String sporsmaal) {
        while(true) {
            System.out.println(sporsmaal + " ");
            String svar = tastatur.nextLine().trim().toLowerCase();
            if (svar.charAt(0) == 'j') return true;
            if (svar.charAt(0) == 'n') return false;
        }
    }
}
class GUIDialog implements Dialog {
    JFrame vindu = null;
    JPanel panel;
    JLabel tekstfelt;
    JButton jaknapp, neiknapp;
    Thread hovedtrad = Thread.currentThread();
    boolean svaret  = true;
    @Override
    public boolean svarJaEllerNei(String sporsmaal) {
        if (vindu == null) {
            vindu = new JFrame("ja eller nei");
            vindu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            panel = new JPanel();
            vindu.add(panel);

            tekstfelt = new JLabel(sporsmaal);
            panel.add(tekstfelt);

            class SvarJaNei implements ActionListener {
                boolean svar;
                public SvarJaNei(boolean jn) {
                    svar = jn;
                }
                @Override
                public void actionPerformed (ActionEvent e) {
                    svaret = svar;
                    hovedtrad.interrupt();
                }
            }
            jaknapp = new JButton("JA");
            jaknapp.addActionListener(new SvarJaNei(true));
            panel.add(jaknapp);

            neiknapp = new JButton("nei");
            panel.add(neiknapp);

            vindu.pack();
            vindu.setVisible(true);
        } else {
            tekstfelt.setText(sporsmaal);
        }
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {}
        return svaret;
    }
}
class Bilkollektiv {
    final int AB;
    Bil[] allebilene;
    Bil start, slutt;
    public Bilkollektiv(int antall) {
        AB = antall;
        allebilene = new Bil[AB];
        start = slutt = null;
    }
    public void lagBilPris() {
        int minForrigePris = -1;
        for (int n = 1; n <= AB; n++) {
            Bil billigst = null;
            for (int i = 0; i <AB; i++) {
                Bil b = allebilene[i];
                if (b.pris > minForrigePris && (billigst == null || b.pris < billigst.pris)) {
                    billigst = b;
                }
            }
            if (start == null) {
                start = slutt = billigst;
            } else {
                slutt.neste = billigst;
                billigst.forrige = slutt;
                slutt = billigst;
            }
            minForrigePris = billigst.pris;
        }
    }
    public void taUtBil(Bil b) {
        if (b == start && start == slutt) {
            start = slutt = null;
        } else if ( b == start ) {
            start = start.neste;
            start.forrige = null;
        } else if (b == slutt) {
            slutt = slutt.forrige;
            slutt.forrige = null;
        } else {
            b.forrige.neste = b.neste;
            b.neste.forrige = b.forrige;
        }
        b.neste = b.forrige = null;
    }
    public Bil velgBil(Dialog d) {
        boolean kunElbil = d.svarJaEllerNei("er du bare intressert i elbil?");
        Bil b = start;
        while(b != null) {
            if (b.erElbil() || !kunElbil) {
                if (d.svarJaEllerNei("liker du " + b + "?")) {
                    taUtBil(b);
                    return b;
                }
            }
            b = b.neste;
        }
        return null;
    }
    public Bil velgBilR(Dialog d) {
        boolean kunElbil = d.svarJaEllerNei("er du bare intressert i elbil?");
        Bil b = start.finnBilR(d, kunElbil);
        if (b != null) taUtBil(b);
        return b;
    }
    public static void main (String[] arg) {
        Bilkollektiv kol = new Bilkollektiv(3);
        kol.allebilene[0] = new PersonBil("AA00001", 350, 4);
        kol.allebilene[1] = new ElektriskVarebil("AA00002", 745, 21, 50);
        kol.allebilene[2] = new ElektriskPersonBil("AA00003", 310, 3, 45);
        kol.lagBilPris();

        Dialog d = new TastaturDialog();
        // Dialog d = new GUIDialog();
        for (int i = 1;  i <= 3;  ++i) {
            // Bil b = kol.velgBil(d);
            Bil b = kol.velgBilR(d);
            if (b == null)
                System.out.println("Ingen bil passet.");
            else
                System.out.println("Bil nr " + i + " er " + b + ".");
        }
        System.exit(0);
    }
}