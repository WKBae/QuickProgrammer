/**
 * 프로그램의 실행을 관리하는 패키지입니다.<br>
 * 프로그램은 {@link net.wkbae.quickprogrammer.runner.ProgramRunner#createRunner(net.wkbae.quickprogrammer.Program)}{@link net.wkbae.quickprogrammer.runner.ProgramRunner#run() .run()}을 통해서 실행됩니다.<br>
 * 프로그램은 별개의 스레드에서 실행되며, 관리를 위해 {@link net.wkbae.quickprogrammer.runner.ProgramThreadGroup}을 이용합니다.<br>
 * 다음 버전에서는 이를 이용한 보안 관리(파일 읽기/쓰기, 소켓 열기/접속 등 제한)가 이루어질 예정입니다.
 */
package net.wkbae.quickprogrammer.runner;