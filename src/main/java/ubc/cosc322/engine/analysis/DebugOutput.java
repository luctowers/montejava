package ubc.cosc322.engine.analysis;

public enum DebugOutput {

	NONE(false, false), ALL(true, true), OUTCOME(false, true);

	public final boolean outputAll;
	public final boolean outputOutcome;

	private DebugOutput(boolean outputAll, boolean outputOutcome) {
		this.outputAll = outputAll;
		this.outputOutcome = outputOutcome;
	}

}
