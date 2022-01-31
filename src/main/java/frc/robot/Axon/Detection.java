package frc.robot.Axon;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Detection {
	@JsonProperty
	private Double confidence;
	@JsonProperty
	private String label;
	@JsonProperty
	private Box box;

	public Detection() {
		super();
	}

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

}
