package net.wkbae.quickprogrammer;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * 파일 선택기({@link JFileChooser})에서 사용되는 필터입니다.<br>
 * 특정 확장자를 가진 파일만 표시되게 합니다.
 * @author WKBae
 */
class FileExtensionFilter extends FileFilter {
	private String ext, desc;
	/**
	 * 확장자 필터를 생성합니다.
	 * @param extension 골라낼 확장자입니다. "."은 붙이지 마세요.("a.exe" -> "exe")<br>
	 * 확장자로 "*"을 사용할 수 있습니다. 이 경우, 모든 파일이 나타납니다.
	 * @param description 확장자에 대한 설명입니다.
	 */
	public FileExtensionFilter(String extension, String description) {
		this.ext = extension.equals("*")? null : ("." + extension);
		this.desc = description;
	}
	
	@Override
	public boolean accept(File f) {
		if(ext == null) return true;
		if(f.isDirectory()) return true;
		return f.getName().toLowerCase().endsWith(ext);
	}
	
	@Override
	public String getDescription() {
		return desc;
	}
	
	/**
	 * 이 필터에 설정된 확장자를 얻습니다.
	 * @return 설정된 확장자, 맨 앞에 "."이 붙습니다. "*"이었다면 <code>null</code>을 반환합니다.
	 */
	public String getExtension() {
		return ext;
	}
}