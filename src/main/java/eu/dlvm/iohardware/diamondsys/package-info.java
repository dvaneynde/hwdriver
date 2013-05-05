/**
 * Abstraction of the Hardware - being a CPU board and a number of I/O cards - through which users can set digital or analog output, and get digital or analog inputs.
 * <p>Users of this Hardware abstraction should only know about a {@link IHardwareIO} implementation, realize themselves {@link IScanEventListener} and use {@link LogCh}.
 * <p>Class {@link OpalmmBoard} shows what I/O boards are capable of only to be used internally, not for users.
 */
package eu.dlvm.iohardware.diamondsys;

import eu.dlvm.iohardware.IHardwareIO;
import eu.dlvm.iohardware.LogCh;

/*
 * Probleem LogCh vs. PhysCh.
 * 
 * 
 * Wie weet hoe die conversie moet gebeuren? We kunnen het laten berekenen
 * volgens een vast algoritme in HwAbstraction, maar dat gaat fout gaan bij
 * DMMAT, of wordt heel ingewikkeld - en in essentie verhuis je het probleem
 * naar documentatie, dus buiten de code ! Je krijgt dus een gemeenschappelijke
 * dependency op documentatie, wat niet afgedwongen kan worden, en wat je moet
 * weten buiten de code.
 * 
 * Alternatief: Blocks weet over Boards, PhysCh. Extra dependency, maar enkel
 * neerwaarts. Kan minder erg worden door een algemene abstractie: board =
 * address digitaal is 1 kanaal, ja of neen aanwezig analoog is n kanalen, met
 * int value beide kan zowel in als out --> dit komt op onze Msg abstractie neer
 * ! Misschien is het dus een vals probleem. Wat in msg zit is nu eenmaal wat we
 * aankunnen, dus ook hardware abstracties. Maar blocks hoeven dat eigenlijk
 * helemaal niet te weten - zal hen worst wezen. Mapping daarom in concrete
 * component van hwabstraction.diamondsystems afzonderen.
 * 
 * Poging dus: 1. Blocks weten nog steeds niets van PhysCh 2. hwabstraction
 * package basisklassen worden afgestemd op Msg; concreet, een OpalmmBoard met
 * bovenstaande aannames, en b.v. een generateInitBoard() operatie per board 3.
 * het mappen van logische naar physische addressen gebeurt in hwabstraction,
 * via abstracte basisklasse daarin; kan later dus ook via xml bestand
 * geconfigureerd
 */
