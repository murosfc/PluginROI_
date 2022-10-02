/* Implementar um plugin para identificar as ROIs existentes em uma imagem e salvar cada uma destas como uma imagem individual no sistema de arquivos.

Pegando como exemplo as imagens microscópicas analisadas na tarefa da segunda aula, é como se gravássemos cada uma das células detectadas na amostra como uma imagem independente no sistema de arquivos.

Cada aluno deverá utilizar a imagem indicada na sua planilha de trabalhos.
Imagens disponíveis em: https://drive.google.com/drive/folders/1-NYz11oA-xAyQrFHsuW07jTyQxPgf3nQ?usp=sharingSugestão de algoritmo:

Obter a imagem original
Utilizar a ferramenta doWand (varinha mágica) para selecionar a borda 
Apagar a borda com o comando clear
Transformar a imagem para 8-Bits
Realizar o Threshold na imagem
Aplicar a morfologia matemática necessária para fechar os contornos das células
Preencher os núcleos das ROIs identificadas
Fazer a separação das células coladas utilizando a técnica de watershed
Executar o comando Analyze Particles para identificar automaticamente as ROIs presentes na imagem, adicioná-los no RoiManager e gerar a máscara da imagem analisada.
Aplicar a máscara da imagem analisada na imagem original RGB.
Criar um vetor contendo todos os ROIs presentes no RoiManager
Colocar a imagem RGB cuja máscara foi aplicada, abaixo do overlay de análise
Obter o caminho para onde os arquivos serão gravados

Para todas as ROIs existentes no vetor
      Obter para cada os seus retângulos de contorno
      Duplicar a imagem
      Realizar o crop na imagem duplicada para o retângulo obtido
      Gravar a imagem recortada

Obs:

Está liberado o uso de comandos de alto nível como Make Binary,  Fill Holes e Analyze Particles...,  que poderão ser executados com IJ.run . */

import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import ij.plugin.frame.ThresholdAdjuster;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.FileSaver;

import java.awt.AWTEvent;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;

import ij.IJ;
import ij.process.ImageProcessor;

public class PluginROI_ implements PlugIn{
	private ImagePlus imp;
	ImageProcessor processorBackup;
	
	public void run(String arg) {	
		imp = IJ.getImage();
		processorBackup = imp.getProcessor().duplicate();
		//limpar as bordas colocando o fundo como branco
		IJ.doWand(imp, 5, 5, 40.0, "Legacy");
		IJ.setForegroundColor(255, 255, 255);
		IJ.run(imp, "Fill", "slice");
		IJ.doWand(imp, imp.getWidth() - 5, 5, 40.0, "Legacy");
		IJ.run(imp, "Fill", "slice");
		IJ.run(imp, "Select None", "");
		//convertendo a imagem para binária
		IJ.run(imp, "Convert to Mask", "");
		//aplicando threshold
		IJ.setRawThreshold(imp, 90, 255);
		IJ.setAutoThreshold(imp, "Default dark no-reset");
		ThresholdAdjuster.setMode("B&W");
		Prefs.blackBackground = true;
		IJ.run(imp, "Convert to Mask", "");
		//fechando os contornos das células
		IJ.run(imp, "Dilate", "");
		IJ.run(imp, "Fill Holes", "");
		IJ.run(imp, "Erode", "");		
		//Pegar as ROIs
		IJ.run(imp, "Analyze Particles...", "size=400-1800 circularity=0.50-1.00 clear add");
		//inverter a imagem para colocar o fundo branco
		IJ.run(imp, "Invert", "");
		exportImages();
	}	
	
	private void exportImages() {
		//carrega a imagem original a partir do processador de backup
		ImagePlus imageOriginal = new ImagePlus();
		imageOriginal.setProcessor(processorBackup);
		//cira a imagem de saída somando a imagem processada com a original
		ImagePlus outputImage = ImageCalculator.run(imp, imageOriginal, "Add create");		
		String outputPath = "C:\\Users\\murosfc\\Downloads\\";
		//seta os Rois na nova imagem criada
		RoiManager roiManager = RoiManager.getRoiManager();						
		for (Roi roi : roiManager) {
			outputImage.setRoi(roi);
		}
		//exporta as imagens em formato tiff
		outputImage.cropAndSave(roiManager.getRoisAsArray(), outputPath, "tif");
	}
} 
	