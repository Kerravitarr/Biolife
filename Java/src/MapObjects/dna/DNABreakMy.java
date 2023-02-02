package MapObjects.dna;

import MapObjects.AliveCell;
import Utils.MyMessageFormat;

/**
 * Команда ломает заменяяет один из будущих генов себя
 * @author Kerravitarr
 *
 */
public class DNABreakMy extends DNABreak {
	
	private final MyMessageFormat param1Format = new MyMessageFormat("PC += {0}");
	private final MyMessageFormat param2Format = new MyMessageFormat("CMD = {0}");
	/**Ломает ДНК того, на кого смотит на определённый*/
	public DNABreakMy() {super(2);isInterrupt = false;};
	@Override
	protected void doing(AliveCell cell) {
		int ma = param(cell,0); // Индекс гена
		int mc = param(cell,1); // Его значение
		breakDNA(cell,ma,mc);
	}
	
	@Override
	public String getParam(AliveCell cell, int numParam, DNA dna) {
		if(numParam == 0){
			return param1Format.format(param(dna,numParam));
		}else {
			return param2Format.format(CommandList.list[param(dna,numParam)]);
		}
	}
}
